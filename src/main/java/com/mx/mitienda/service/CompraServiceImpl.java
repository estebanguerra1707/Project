package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.util.CompraSpecBuilder;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.Utils.*;

@Slf4j
@Service
public class CompraServiceImpl extends BaseService implements ICompraService {
    public final CompraRepository compraRepository;
    public final CompraMapper compraMapper;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final IHistorialMovimientosService historialMovimientosService;
    private final IAlertaCorreoService alertaCorreoService;
    private final ProductoRepository productoRepository;
    private final IGeneratePdfService generatePdfService;
    private final MailService mailService;
    private final DetalleDevolucionComprasRepository detalleDevolucionComprasRepository;
    private final DevolucionComprasRepository devolucionComprasRepository;

    @Value("${alertas.stock.email.destinatario}")
    private String destinatario;

    public CompraServiceImpl(
            IAuthenticatedUserService authenticatedUserService,
            CompraRepository compraRepository,
            CompraMapper compraMapper,
            InventarioSucursalRepository inventarioSucursalRepository,
            IHistorialMovimientosService historialMovimientosService,
            IAlertaCorreoService alertaCorreoService,
            ProductoRepository productoRepository,
            IGeneratePdfService generatePdfService,
            MailService mailService,
            DetalleDevolucionComprasRepository detalleDevolucionComprasRepository,
            DevolucionComprasRepository devolucionComprasRepository
    ) {
        super(authenticatedUserService);
        this.compraRepository = compraRepository;
        this.compraMapper = compraMapper;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.historialMovimientosService = historialMovimientosService;
        this.alertaCorreoService = alertaCorreoService;
        this.productoRepository = productoRepository;
        this.generatePdfService = generatePdfService;
        this.mailService = mailService;
        this.detalleDevolucionComprasRepository = detalleDevolucionComprasRepository;
        this.devolucionComprasRepository = devolucionComprasRepository;
    }

    public List<CompraResponseDTO> getAll(String username, String role){
        UserContext ctx = ctx();
        if (ctx.isSuperAdmin()) {
            return compraRepository.findByActiveTrueOrderByIdAsc()
                    .stream()
                    .map(compraMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            return compraRepository.findByBranch_IdAndActiveTrue(ctx.getBranchId())
                    .stream()
                    .map(compraMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }



    @Override
    public CompraResponseDTO getById(Long idPurchase){
        UserContext ctx = ctx();
        Compra compra;
        if (ctx.isSuperAdmin()) {
            compra = compraRepository.findByIdAndActiveTrue(idPurchase)
                    .orElseThrow(() -> new NotFoundException("Compra no encontrada"));
        } else {
            compra = compraRepository.findByIdAndBranch_IdAndActiveTrue(idPurchase, ctx.getBranchId())
                    .orElseThrow(() -> new NotFoundException(
                            "La compra no se ha encontrado dentro de la sucursal asignada al usuario logueado"));
        }
        return compraMapper.toResponse(compra);

    }

    @Override
    @Transactional
    public CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth) {

        String username = auth.getName();
        boolean esVendor = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_VENDOR".equals(a.getAuthority()));

        LocalDateTime hoy = LocalDateTime.now();
        if (esVendor) {
            if (compraRequestDTO.getPurchaseDate() == null ||
                    !compraRequestDTO.getPurchaseDate().toLocalDate().equals(hoy)) {
                throw new IllegalArgumentException("Solo puedes registrar compras el día de hoy");
            }
        }

        Compra compra = compraMapper.toEntity(compraRequestDTO, username);
        Compra compraGuardada = compraRepository.saveAndFlush(compra);

        // Primero validamos y actualizamos inventarios
        for (DetalleCompra detalle : compraGuardada.getDetails()) {
            Producto producto = productoRepository.findById(detalle.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

            InventarioSucursal inventarioSucursal = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_Id(producto.getId(), compraGuardada.getBranch().getId())
                    .orElseGet(() -> {
                        InventarioSucursal inv = new InventarioSucursal();
                        inv.setProduct(producto);
                        inv.setBranch(compraGuardada.getBranch());
                        inv.setStock(0);
                        return inv;
                    });

            int stockAnterior = Optional.ofNullable(inventarioSucursal.getStock()).orElse(0);
            int stockNuevo = stockAnterior + detalle.getQuantity();

            Integer max = inventarioSucursal.getMaxStock();
            if (max != null && stockNuevo > max) {
                throw new IllegalArgumentException("La compra excede el inventario máximo permitido para el producto: "
                        + producto.getName());
            }

            inventarioSucursal.setStock(stockNuevo);
            inventarioSucursal.setStockCritico(
                    inventarioSucursal.getMinStock() != null && stockNuevo < inventarioSucursal.getMinStock()
            );
            inventarioSucursal.setLastUpdatedBy(username);
            inventarioSucursal.setLastUpdatedDate(LocalDateTime.now());
            inventarioSucursalRepository.save(inventarioSucursal);

            // Notificación (si realmente quieres notificar cuando sigue crítico tras la compra)
            if (Boolean.TRUE.equals(inventarioSucursal.getStockCritico())
                    && Boolean.TRUE.equals(inventarioSucursal.getBranch().getAlertaStockCritico())) {
                alertaCorreoService.notificarStockCritico(inventarioSucursal);
            } else {
                log.info("La sucursal no tiene alerta de stock crítico por correo o no aplica.");
            }

            historialMovimientosService.registrarMovimiento(
                    inventarioSucursal,
                    TipoMovimiento.ENTRADA,
                    detalle.getQuantity(),
                    stockAnterior,
                    stockNuevo,
                    "Compra #" + compraGuardada.getId());
        }
        //envio por correo
        generatePurchaseEmail(compraGuardada, compraRequestDTO.getEmailList(), compraRequestDTO.isPrinted());
        return compraMapper.toResponse(compraGuardada);
    }

    @Override
    public void inactivePurchase(Long id) {
        Compra compra = compraRepository.findById(id).orElseThrow(()-> new NotFoundException("Compra no encontrada"));
        compra.setActive(false);
        compraRepository.save(compra);
    }

    @Override
    public List<CompraResponseDTO> advancedSearch(CompraFiltroDTO compraDTO){
        Specification<Compra> spec = new CompraSpecBuilder()
                .active(compraDTO.getActive())
                .supplier(compraDTO.getSupplier())
                .dateBetween(compraDTO.getStart(), compraDTO.getEnd())
                .totalMajorTo(compraDTO.getMin())
                .totalMinorTo(compraDTO.getMax())
                .searchPerDayMonthYear(compraDTO.getDay(), compraDTO.getMonth(), compraDTO.getYear())
                .build();

        Sort sort = Sort.by(Sort.Direction.ASC, "totalAmount");


        return compraRepository.findAll(spec, sort).stream()
                .map(compraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraResponseDTO> findCurrentUserCompras() {
        UserContext ctx = ctx();
        return compraRepository.findByBranchAndBusinessType( ctx.getBranchId(),
                        ctx.getBusinessTypeId())
                .stream()
                .map(compraMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void generatePurchaseEmail(Compra compraGuardada, List<String> emailList, Boolean isPrinted){
        // Generar PDF
        byte[] pdfBytes = generatePdfService.generatePdf(COMPRA_CODE, compraGuardada.getId(), isPrinted != null && isPrinted);

// Enviar por correo
        if (emailList != null && !emailList.isEmpty()) {
            mailService.sendPDFEmail(
                    emailList,
                    "Compra",
                    "Comprobante de " +COMPRA_CODE,
                    "<p>Adjunto encontrarás tu comprobante de compra.</p>",
                    pdfBytes,
                    COMPRA_CODE + "_" + compraGuardada.getId() + ".pdf"
            );
        }
        if (isPrinted != null && isPrinted) {
            log.info(":::Se generó ticket térmico para la compra ID::: {}", compraGuardada.getId());
            // Puedes almacenar temporalmente el PDF térmico o devolverlo directamente si es sincrónico
        }
        if (Boolean.TRUE.equals(isPrinted)) log.info("::Generando ticket térmico para compra::{}", compraGuardada.getId());
        if (emailList != null && !emailList.isEmpty()) log.info("::Enviando PDF de compra a:: {}", emailList);
    }


}
