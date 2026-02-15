package com.mx.mitienda.service;

import com.mx.mitienda.exception.BadRequestException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.specification.CompraSpecification;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.util.CompraSpecBuilder;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final SucursalRepository sucursalRepository;


    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE  = BigDecimal.ONE;

    private BigDecimal nz(BigDecimal v) {
        return v == null ? ZERO : v;
    }

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
            DevolucionComprasRepository devolucionComprasRepository,
            SucursalRepository sucursalRepository
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
        this.sucursalRepository = sucursalRepository;
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
    @Transactional
    public CompraResponseDTO getById(Long idPurchase){
        UserContext ctx = ctx();
        Compra compra;
        if (ctx.isSuperAdmin()) {
            compra = compraRepository.findByIdFull(idPurchase)
                    .orElseThrow(() -> new NotFoundException("Compra no encontrada"));
        } else {
            compra = compraRepository.findByIdFullByBranch(idPurchase, ctx.getBranchId())
                    .orElseThrow(() -> new NotFoundException(
                            "La compra no se ha encontrado dentro de la sucursal asignada al usuario logueado"));
        }
        return compraMapper.toResponse(compra);

    }

    @Override
    @Transactional
    public CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth) {

        UserContext ctx = ctx();

        Long branchIdEfectivo = ctx.isSuperAdmin()
                ? compraRequestDTO.getBranchId()
                : ctx.getBranchId();

        if (branchIdEfectivo == null) {
            throw new BadRequestException("No se pudo determinar la sucursal para la compra");
        }

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchIdEfectivo)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la sucursal"));

        String username = auth.getName();
        boolean esVendor = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_VENDOR".equals(a.getAuthority()));

        LocalDate hoy = LocalDate.now();
        if (esVendor) {
            if (compraRequestDTO.getPurchaseDate() == null ||
                    !compraRequestDTO.getPurchaseDate().toLocalDate().equals(hoy)) {
                throw new IllegalArgumentException("Solo puedes registrar compras el día de hoy");
            }
        }

        Compra compra = compraMapper.toEntity(compraRequestDTO, username);
        compra.setBranch(sucursal);
        Compra compraGuardada = compraRepository.saveAndFlush(compra);

        for (DetalleCompra detalle : compraGuardada.getDetails()) {
            Producto producto = productoRepository.findByIdAndActiveTrue(detalle.getProduct().getId())
                    .orElseThrow(() -> new BadRequestException("Producto no disponible o inactivo"));


            validarCantidadSegunUnidad(producto, detalle.getQuantity());

            boolean usaInventarioPorDuenio =
                    Boolean.TRUE.equals(sucursal.getUsaInventarioPorDuenio());

            InventarioOwnerType ownerType;

            if (usaInventarioPorDuenio) {
                if (detalle.getOwnerType() == null) {
                    throw new BadRequestException(
                            "Debe especificar si el inventario es PROPIO o CONSIGNADO para el producto "
                                    + producto.getName()
                    );
                }
                ownerType = detalle.getOwnerType();
            } else {
                ownerType = InventarioOwnerType.PROPIO;
            }

            InventarioSucursal inventarioSucursal =
                    inventarioSucursalRepository
                            .findByProduct_IdAndBranch_IdAndOwnerType(
                                    producto.getId(),
                                    sucursal.getId(),
                                    ownerType
                            )
                            .orElseGet(() -> {
                                // CREACIÓN EXPLÍCITA DE INVENTARIO
                                InventarioSucursal nuevo = new InventarioSucursal();
                                nuevo.setProduct(producto);
                                nuevo.setBranch(sucursal);
                                nuevo.setOwnerType(ownerType);
                                nuevo.setStock(ZERO);
                                nuevo.setMinStock(ONE);
                                nuevo.setMaxStock(BigDecimal.TEN);
                                nuevo.setLastUpdatedDate(LocalDateTime.now());
                                nuevo.setLastUpdatedBy(ctx.getEmail());
                                nuevo.setStockCritico(false);
                                return nuevo;
                            });

            BigDecimal stockAnterior = nz(inventarioSucursal.getStock());
            BigDecimal qty = nz(detalle.getQuantity());
            BigDecimal stockNuevo = stockAnterior.add(qty);

            BigDecimal max = inventarioSucursal.getMaxStock();

            if (max != null && stockNuevo.compareTo(max) > 0) {
                throw new IllegalArgumentException(
                        "La compra excede el inventario máximo permitido para el producto: "
                                + producto.getName()
                );
            }

            inventarioSucursal.setStock(stockNuevo);

            BigDecimal min = inventarioSucursal.getMinStock();
            inventarioSucursal.setStockCritico(min != null && stockNuevo.compareTo(min) < 0);

            inventarioSucursal.setLastUpdatedBy(username);
            inventarioSucursal.setLastUpdatedDate(LocalDateTime.now());

            inventarioSucursalRepository.save(inventarioSucursal);


            if (Boolean.TRUE.equals(inventarioSucursal.getStockCritico())
                    && Boolean.TRUE.equals(sucursal.getAlertaStockCritico())) {
                alertaCorreoService.notificarStockCritico(inventarioSucursal);
            }


            historialMovimientosService.registrarMovimiento(
                    inventarioSucursal,
                    TipoMovimiento.ENTRADA,
                    qty,
                    stockAnterior,
                    stockNuevo,
                    "Compra #" + compraGuardada.getId()
            );
        }

        generatePurchaseEmail(
                compraGuardada,
                compraRequestDTO.getEmailList(),
                compraRequestDTO.isPrinted()
        );

        return compraMapper.toResponse(compraGuardada);
    }
    @Override
    public void inactivePurchase(Long id) {
        Compra compra = compraRepository.findById(id).orElseThrow(()-> new NotFoundException("Compra no encontrada"));
        compra.setActive(false);
        compraRepository.save(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompraResponseDTO> advancedSearch(CompraFiltroDTO filterDTO, Pageable pageable) {

        UserContext ctx = ctx();

        boolean isSuper = ctx.isSuperAdmin();

        CompraSpecBuilder builder = new CompraSpecBuilder()
                .active(filterDTO.getActive())
                .supplier(filterDTO.getSupplierId())
                .dateBetween(filterDTO.getStart(), filterDTO.getEnd())
                .totalMajorTo(filterDTO.getMin())
                .totalMinorTo(filterDTO.getMax())
                .searchPerDayMonthYear(filterDTO.getDay(), filterDTO.getMonth(), filterDTO.getYear())
                .byId(filterDTO.getPurchaseId());

        // SUPER puede filtrar por username si se lo mandan
        if (isSuper) {
            builder.username(filterDTO.getUsername());
        }

        Specification<Compra> spec = Specification.where(builder.build());

        if (!isSuper) {
            Long branchId = ctx.getBranchId();
            if (branchId == null) {
                throw new IllegalStateException("El usuario ADMIN debe tener branchId");
            }
            spec = spec
                    .and(CompraSpecification.byBranch(branchId))
                    .and(CompraSpecification.byUserRoles("ADMIN", "VENDOR"));
        }

        return compraRepository.findAll(spec, pageable)
                .map(compraMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional
    public void deleteLogical(Long id) {

        UserContext ctx = ctx();
        Compra compra;
        if (!ctx.isSuperAdmin()) {
            throw new BadRequestException("No puede borrar compras");
        }
        compra = compraRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Compra no encontrada"));

        if (!compra.getActive()) {
            return;
        }
        compra.setActive(false);

        for (DetalleCompra detalle : compra.getDetails()) {

            Long productId = detalle.getProduct().getId();
            productoRepository.findByIdAndActiveTrue(productId)
                    .orElseThrow(() -> new BadRequestException(
                            "No se puede eliminar la compra: contiene un producto inactivo (ID " + productId + ")"
                    ));
            Long branchId = compra.getBranch().getId();
            InventarioOwnerType ownerType =
                    detalle.getOwnerType() != null
                            ? detalle.getOwnerType()
                            : InventarioOwnerType.PROPIO;

            InventarioSucursal inventario = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_IdAndOwnerType(
                            productId,
                            branchId,
                            ownerType
                    )
                    .orElse(null);

            if (inventario != null) {
                BigDecimal before = nz(inventario.getStock());
                BigDecimal qty = nz(detalle.getQuantity());
                BigDecimal after = before.subtract(qty);
                // 🔒 Seguridad: evitar stock negativo
                if (after.compareTo(ZERO) < 0) {
                    throw new BadRequestException(
                            "Eliminación inválida: el stock quedaría negativo para el producto "
                                    + detalle.getProduct().getName()
                    );
                }

                inventario.setStock(after);
                BigDecimal min = inventario.getMinStock();
                inventario.setStockCritico(min != null && after.compareTo(min) < 0);

                inventario.setLastUpdatedBy(ctx.getEmail());
                inventario.setLastUpdatedDate(LocalDateTime.now());

                inventarioSucursalRepository.save(inventario);

                // 🧾 Registrar movimiento
                historialMovimientosService.registrarMovimiento(
                        inventario,
                        TipoMovimiento.SALIDA,
                        detalle.getQuantity(),
                        before,
                        after,
                        "Eliminación de compra #" + compra.getId()
                );
            }
        }

        compraRepository.save(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompraResponseDTO> findByBranchPaginated(Long branchId, int page, int size, String sort) {
        UserContext ctx = ctx();
        String sortField = "purchaseDate";
        Sort.Direction direction = Sort.Direction.DESC; // default

        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            sortField = parts[0];

            if (parts.length > 1) {
                direction = "asc".equalsIgnoreCase(parts[1])
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Compra> comprasPage;
        if (ctx.isSuperAdmin()) {
            comprasPage = compraRepository.findAllWithDetails(pageable);
        }

        else {
            if (branchId == null) {
                throw new IllegalArgumentException("El usuario no tiene una sucursal asignada");
            }
            comprasPage = compraRepository.findByBranchIdAndActiveTrue(branchId, pageable);
        }

        return comprasPage.map(compraMapper::toResponse);
    }

    private void validarCantidadSegunUnidad(Producto producto, BigDecimal qty) {
        if (qty == null || qty.compareTo(ZERO) <= 0) {
            throw new BadRequestException("Cantidad inválida para el producto: " + producto.getName());
        }
        boolean permiteDecimales = producto.isPermiteDecimales();
        if (!permiteDecimales) {
            if (qty.stripTrailingZeros().scale() > 0) {
                throw new BadRequestException(
                        "El producto " + producto.getName() + " no permite decimales por su unidad de medida."
                );
            }
        }
    }
}
