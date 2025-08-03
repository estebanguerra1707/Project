package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.Utils.COMPRA_CODE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements ICompraService {
    public final CompraRepository compraRepository;
    public final CompraMapper compraMapper;
    private final IAuthenticatedUserService authenticatedUserService;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final IHistorialMovimientosService historialMovimientosService;
    private final IAlertaCorreoService alertaCorreoService;
    private final ProductoRepository productoRepository;
    private final IGeneratePdfService generatePdfService;
    private final MailService mailService;

    @Value("${alertas.stock.email.destinatario}")
    private String destinatario;

    public List<CompraResponseDTO> getAll(String username, String role){
        if(role.equals(Rol.ADMIN) || authenticatedUserService.isSuperAdmin()){
            return compraRepository.findByActiveTrueOrderByIdAsc().stream()
                    .map(compraMapper::toResponse)
                    .collect(Collectors.toList());

        }else{
            return compraRepository.findByUsuario_UsernameAndActiveTrue(username).stream()
                    .map(compraMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }


    public CompraResponseDTO getById(Long idPurchase){
        Long branchId = authenticatedUserService.getCurrentBranchId();
        if(authenticatedUserService.isSuperAdmin()){
            Compra compra = compraRepository.findByIdAndActiveTrue(idPurchase).orElseThrow(()->(new NotFoundException("La compra con el id:: " +idPurchase+" no se ha encontrado")));
            return compraMapper.toResponse(compra);
        }
        Compra compra = compraRepository.findByIdAndBranch_IdAndActiveTrue(idPurchase,branchId).orElseThrow(()->(new NotFoundException("La compra no se ha encontrado dentro de la sucursal con el usuario loggeado, intenta con otro")));
        return compraMapper.toResponse(compra);

    }

    @Transactional
    public CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth) {

        String username = auth.getName();
        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(granted -> granted.getAuthority().replace("ROLE_", ""))
                .orElse("");

        if ("VENDOR".equalsIgnoreCase(rol)) {
            if (compraRequestDTO.getPurchaseDate() == null ||
                    !compraRequestDTO.getPurchaseDate().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                throw new IllegalArgumentException("Solo puedes registrar compras el día de hoy");
            }
        }

        Compra compra = compraMapper.toEntity(compraRequestDTO, username);

        // Primero validamos y actualizamos inventarios
        for (DetalleCompra detalle : compra.getDetails()) {
            Producto producto = productoRepository.findById(detalle.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
            InventarioSucursal inventarioSucursal = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_Id(detalle.getProduct().getId(), compra.getBranch().getId())
                    .orElse(new InventarioSucursal());

            inventarioSucursal.setProduct(detalle.getProduct());
            inventarioSucursal.setBranch(compra.getBranch());

            int stockAnterior = inventarioSucursal.getStock() != null ? inventarioSucursal.getStock() : 0;
            int stockNuevo = stockAnterior + detalle.getQuantity();

            if (inventarioSucursal.getMaxStock() != null && stockNuevo > inventarioSucursal.getMaxStock()) {
                throw new IllegalArgumentException("La compra excede el inventario máximo permitido para el producto: "
                        + detalle.getProduct().getName());
            }

            inventarioSucursal.setStockCritico(stockNuevo < inventarioSucursal.getMinStock());
            if (inventarioSucursal.getStockCritico() && inventarioSucursal.getBranch().getAlertaStockCritico()) {
                alertaCorreoService.notificarStockCritico(inventarioSucursal);
                }else{
                    log.info(":::La sucursal no cuenta con notificaciones de stock critico enviadas por correo:::");
                    System.out.print(":::La sucursal no cuenta con notificaciones de stock critico enviadas por correo:::");
                }

            inventarioSucursal.setStock(stockNuevo);
            inventarioSucursal.setLastUpdatedBy(username);
            inventarioSucursal.setLastUpdatedDate(LocalDateTime.now());

            inventarioSucursalRepository.save(inventarioSucursal);

            // registrar historial
            historialMovimientosService.registrarMovimiento(
                    inventarioSucursal,
                    TipoMovimiento.ENTRADA,
                    detalle.getQuantity(),
                    stockAnterior,
                    stockNuevo,
                    "Compra #" + (compra.getId() != null ? compra.getId() : "pendiente")
            );
        }

        // Guardar la compra después del inventario (así evitamos guardar si algo sale mal)
        Compra compraGuardada = compraRepository.save(compra);
        //envio por correo
        generatePurchaseEmail(compraGuardada, compraRequestDTO.getEmailList(), compraRequestDTO.isPrinted());
        return compraMapper.toResponse(compraGuardada);
    }

    public void inactivePurchase(Long id) {
        Compra compra = compraRepository.findById(id).orElseThrow(()-> new NotFoundException("Compra no encontrada"));
        compra.setActive(false);
        compraRepository.save(compra);
    }

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

    public List<CompraResponseDTO> findCurrentUserCompras() {
        Long branchId = authenticatedUserService.getCurrentBranchId();
        Long businessTypeId = authenticatedUserService.getCurrentBusinessTypeId();

        return compraRepository.findByBranchAndBusinessType(branchId, businessTypeId)
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
