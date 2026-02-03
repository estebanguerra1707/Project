package com.mx.mitienda.service;

import com.mx.mitienda.exception.BadRequestException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.enums.BusinessTypeEnum;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.util.CompraSpecBuilder;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
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
            compra = compraRepository.findByIdAndActiveTrue(idPurchase)
                    .orElseThrow(() -> new NotFoundException("Compra no encontrada"));
        } else {
            compra = compraRepository.findByIdAndBranch_IdAndActiveTrue(idPurchase, ctx.getBranchId())
                    .orElseThrow(() -> new NotFoundException(
                            "La compra no se ha encontrado dentro de la sucursal asignada al usuario logueado"));
        }
        compra.getDetails().size();
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
        Compra compraGuardada = compraRepository.saveAndFlush(compra);

        for (DetalleCompra detalle : compraGuardada.getDetails()) {
            Producto producto = productoRepository.findByIdAndActiveTrue(detalle.getProduct().getId())
                    .orElseThrow(() -> new BadRequestException("Producto no disponible o inactivo"));

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
                                nuevo.setStock(0);
                                nuevo.setMinStock(1);
                                nuevo.setMaxStock(10);
                                nuevo.setLastUpdatedDate(LocalDateTime.now());
                                nuevo.setLastUpdatedBy(ctx.getEmail());
                                nuevo.setStockCritico(false);
                                return nuevo;
                            });


            int stockAnterior = Optional.ofNullable(inventarioSucursal.getStock()).orElse(0);
            int stockNuevo = stockAnterior + detalle.getQuantity();

            Integer max = inventarioSucursal.getMaxStock();
            if (max != null && stockNuevo > max) {
                throw new IllegalArgumentException(
                        "La compra excede el inventario máximo permitido para el producto: "
                                + producto.getName()
                );
            }

            inventarioSucursal.setStock(stockNuevo);
            inventarioSucursal.setStockCritico(
                    inventarioSucursal.getMinStock() != null
                            && stockNuevo < inventarioSucursal.getMinStock()
            );
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
                    detalle.getQuantity(),
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
    public Page<CompraResponseDTO> advancedSearch(CompraFiltroDTO compraDTO, Pageable pageable) {
        Specification<Compra> spec = new CompraSpecBuilder()
                .active(compraDTO.getActive())
                .supplier(compraDTO.getSupplierId())
                .dateBetween(compraDTO.getStart(), compraDTO.getEnd())
                .totalMajorTo(compraDTO.getMin())
                .totalMinorTo(compraDTO.getMax())
                .searchPerDayMonthYear(compraDTO.getDay(), compraDTO.getMonth(), compraDTO.getYear())
                .byId(compraDTO.getPurchaseId())
                .excludeIfAnyInactiveProduct()
                .build();

        // 🔹 Usa el método con paginación
        Page<Compra> compras = compraRepository.findAll(spec, pageable);

        // 🔹 Forzar carga de los detalles dentro de la sesión activa
        compras.forEach(c -> Hibernate.initialize(c.getDetails()));

        // 🔹 Mapea y devuelve como Page<CompraResponseDTO>
        return compras.map(compraMapper::toResponse);
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

                int before = inventario.getStock();
                int after = before - detalle.getQuantity();

                // 🔒 Seguridad: evitar stock negativo
                if (after < 0) {
                    throw new BadRequestException(
                            "Eliminación inválida: el stock quedaría negativo para el producto "
                                    + detalle.getProduct().getName()
                    );
                }

                inventario.setStock(after);
                inventario.setStockCritico(
                        inventario.getMinStock() != null && after < inventario.getMinStock()
                );
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

        // -----------------------------
        // 🔹 PARSEAR SORT "campo,direccion"
        // -----------------------------
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

        // -----------------------------
        // 🔹 SUPER_ADMIN → ve todo
        // -----------------------------
        if (ctx.isSuperAdmin()) {
            comprasPage = compraRepository.findAllWithDetails(pageable);
        }
        // -----------------------------
        // 🔹 ADMIN / VENDOR → filtra por sucursal
        // -----------------------------
        else {
            if (branchId == null) {
                throw new IllegalArgumentException("El usuario no tiene una sucursal asignada");
            }
            comprasPage = compraRepository.findByBranchIdAndActiveTrue(branchId, pageable);
        }

        return comprasPage.map(compraMapper::toResponse);
    }
}
