package com.mx.mitienda.service;

import com.mx.mitienda.exception.BadRequestException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DevolucionComprasMapper;
import com.mx.mitienda.mapper.FiltroDevolucionComprasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DevolucionComprasFiltroDTO;
import com.mx.mitienda.model.dto.DevolucionComprasReponseDTO;
import com.mx.mitienda.model.dto.DevolucionComprasRequestDTO;
import com.mx.mitienda.model.dto.FiltroDevolucionComprasResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.DevolucionComprasSpecBuilder;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.util.enums.TipoDevolucion;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mx.mitienda.util.Utils.*;

@Service
public class DevolucionComprasServiceImpl extends BaseService implements IDevolucionComprasService {

    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;
    private final DetalleDevolucionComprasRepository detalleDevolucionComprasRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final HistorialMovimientoRepository historialMovimientoRepository;
    private final DevolucionComprasMapper devolucionComprasMapper;
    private final DevolucionComprasRepository devolucionComprasRepository;
    private final SucursalRepository sucursalRepository;
    private final FiltroDevolucionComprasMapper filtroDevolucionComprasMapper;

    public DevolucionComprasServiceImpl(ProductoRepository productoRepository, CompraRepository compraRepository,
                                        DetalleDevolucionComprasRepository detalleDevolucionComprasRepository,
                                        InventarioSucursalRepository inventarioSucursalRepository,
                                        AuthenticatedUserServiceImpl authenticatedUserService,
                                        HistorialMovimientoRepository historialMovimientoRepository,
                                        DevolucionComprasMapper devolucionComprasMapper,
                                        DevolucionComprasRepository devolucionComprasRepository, SucursalRepository sucursalRepository,
                                        FiltroDevolucionComprasMapper filtroDevolucionComprasMapper) {
        super(authenticatedUserService);
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.detalleDevolucionComprasRepository = detalleDevolucionComprasRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.historialMovimientoRepository = historialMovimientoRepository;
        this.devolucionComprasMapper = devolucionComprasMapper;
        this.devolucionComprasRepository = devolucionComprasRepository;
        this.sucursalRepository = sucursalRepository;
        this.filtroDevolucionComprasMapper = filtroDevolucionComprasMapper;
    }

    @Override
    @Transactional
    public DevolucionComprasReponseDTO procesarDevolucion(DevolucionComprasRequestDTO req) {

        UserContext ctx = ctx();
        Long branchId = ctx.isSuperAdmin()
                ? req.getBranchId()
                : ctx.getBranchId();

        Long businessId =  ctx.isSuperAdmin()
                ? req.getBusinessTypeId()
                : ctx.getBusinessTypeId();

        Usuario usuario = authenticatedUserService.getCurrentUser();

        // 1) Compra validada según rol
        Compra compra = obtenerCompraValida(req.getCompraId(), branchId);
        // 2) Producto correcto según tipo de negocio
        Producto producto = obtenerProductoValido(
                req.getCodigoBarras(),
                businessId
        );
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        // 3) Detalle correcto dentro de la compra
        DetalleCompra detalleCompra = obtenerDetalleCompra(compra, producto.getId());
        BigDecimal cantidadComprada = Optional.ofNullable(detalleCompra.getQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal cantidadSolicitada = Optional.ofNullable(req.getCantidad()).orElse(BigDecimal.ZERO);

        if (cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Cantidad inválida para devolución.");
        }

        if (cantidadSolicitada.compareTo(cantidadComprada) > 0) {
            throw new BadRequestException("No puedes devolver más de " + cantidadComprada + " unidades.");
        }

        BigDecimal devueltaAcum = Optional.ofNullable(
                detalleDevolucionComprasRepository
                        .sumCantidadDevueltaPorCompraYProducto(compra.getId(), producto.getId())
        ).orElse(BigDecimal.ZERO);

        BigDecimal cantidadRestante = cantidadComprada.subtract(devueltaAcum);

        if (cantidadSolicitada.compareTo(cantidadRestante) > 0) {
            throw new IllegalArgumentException("No puedes devolver más de lo comprado");
        }

        // 4) Inventario validado
        InventarioSucursal inventario = obtenerInventario(
                producto,
                sucursal,
                detalleCompra
        );

        BigDecimal stockActual = Optional.ofNullable(inventario.getStock()).orElse(BigDecimal.ZERO);

        if (stockActual.compareTo(cantidadSolicitada) < 0) {
            throw new IllegalArgumentException("No hay stock suficiente para devolver al proveedor");
        }

        // 5) Construcción de devolución
        DevolucionCompras devolucion = devolucionComprasMapper.toEntity(
                req,
                compra,
                detalleCompra,
                usuario,
                sucursal
        );

        boolean devolucionTotal =
                devueltaAcum.add(cantidadSolicitada).compareTo(cantidadComprada) == 0;
        devolucion.setTipoDevolucion(devolucionTotal ? TipoDevolucion.TOTAL : TipoDevolucion.PARCIAL);


        devolucion.getDetalles().forEach(d -> d.setDevolucion(devolucion));
        devolucionComprasRepository.save(devolucion);

        // 6) Actualizar inventario (SALIDA)
        BigDecimal before = Optional.ofNullable(inventario.getStock()).orElse(BigDecimal.ZERO);
        BigDecimal after = before.subtract(cantidadSolicitada);;

        inventario.setStock(after);
        inventarioSucursalRepository.save(inventario);

        // 7) Historial
        registrarMovimiento(inventario, cantidadSolicitada, before, after, compra.getId());

        return devolucionComprasMapper.toResponse(devolucion);
    }

    // ----------------------------------------
    // REPORTES
    // ----------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> contarPorDia(LocalDate desde, LocalDate hasta, Long branchId) {
        validarRango(desde, hasta);
        var result = devolucionComprasRepository.countPorDia(branchId, inicioDelDia(desde), finDelDia(hasta));
        var map = prellenarDiasConCero(desde, hasta);
        result.forEach(o -> map.put(toLocalDate(o[0]), ((Number) o[1]).longValue()));
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> contarPorSemana(LocalDate desde, LocalDate hasta, Long branchId) {
        validarRango(desde, hasta);
        var result = devolucionComprasRepository.countPorSemana(branchId, inicioDelDia(desde), finDelDia(hasta));
        var map = prellenarSemanasConCero(desde, hasta);
        result.forEach(o -> map.put(toLocalDate(o[0]), ((Number) o[1]).longValue()));
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<YearMonth, Long> contarPorMes(LocalDate desde, LocalDate hasta, Long branchId) {
        validarRango(desde, hasta);
        var result = devolucionComprasRepository.countPorMes(branchId, inicioDelDia(desde), finDelDia(hasta));
        var map = prellenarMesesConCero(desde, hasta);
        result.forEach(o -> map.put(toYearMonth(o[0]), ((Number) o[1]).longValue()));
        return map;
    }

    @Override
    public BigDecimal obtenerDevolucionesComprasPorRango(LocalDate desde, LocalDate hasta) {
        Long branchId = authenticatedUserService.getCurrentBranchId();
        return detalleDevolucionComprasRepository.sumMontoDevueltoCompras(
                branchId, inicioDelDia(desde), finDelDia(hasta)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FiltroDevolucionComprasResponseDTO> advancedSearch(
            DevolucionComprasFiltroDTO filtro,
            Pageable pageable
    ) {
        Long branchId = ctx().isSuperAdmin()
                ? filtro.getBranchId()
                : ctx().getBranchId();

        var spec = new DevolucionComprasSpecBuilder()
                .byId(filtro.getDevolucionId())
                .byCompra(filtro.getCompraId())
                .byCodigoBarras(filtro.getCodigoBarras())
                .byUsuario(filtro.getUsername())
                .byTipo(filtro.getTipoDevolucion())
                .dateBetween(filtro.getStart(), filtro.getEnd())
                .searchPerDayMonthYear(filtro.getDay(), filtro.getMonth(), filtro.getYear())
                .montoBetween(filtro.getMinMonto(), filtro.getMaxMonto())
                .cantidadBetween(filtro.getMinCantidad(), filtro.getMaxCantidad())
                .branch(branchId)
                .build();

        Page<DevolucionCompras> pageResult = devolucionComprasRepository.findAll(spec, pageable);

        return pageResult.map(filtroDevolucionComprasMapper::toDto);
    }

    // ----------------------------------------
    // HELPERS PRIVADOS (limpios y reutilizables)
    // ----------------------------------------

    private Compra obtenerCompraValida(Long compraId, Long branchId) {
        return compraRepository.findByIdAndBranch_IdAndActiveTrue(compraId, branchId)
                .orElseThrow(() -> new NotFoundException("Compra no encontrada en tu sucursal"));
    }

    private Producto obtenerProductoValido(String codigo, Long businessId) {
        Producto p = productoRepository.findByCodigoBarrasAndBusinessTypeId(codigo, businessId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + codigo));

        if (!Boolean.TRUE.equals(p.getActive())) {
            throw new BadRequestException("Producto inactivo: " + codigo);
        }
        return p;
    }

    private DetalleCompra obtenerDetalleCompra(Compra compra, Long productId) {
        return compra.getDetails()
                .stream()
                .filter(d -> d.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("El producto no está en la compra"));
    }

    private InventarioSucursal obtenerInventario(
            Producto producto,
            Sucursal sucursal,
            DetalleCompra detalleCompra
    ) {

        boolean usaInventarioPorDuenio =
                Boolean.TRUE.equals(sucursal.getUsaInventarioPorDuenio());

        if (usaInventarioPorDuenio) {

            InventarioOwnerType ownerType =
                    detalleCompra.getOwnerType() != null
                            ? detalleCompra.getOwnerType()
                            : InventarioOwnerType.PROPIO;

            return inventarioSucursalRepository
                    .findByProduct_IdAndBranch_IdAndOwnerType(
                            producto.getId(),
                            sucursal.getId(),
                            ownerType
                    )
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Inventario no encontrado para el producto "
                                            + producto.getName()
                                            + " con dueño " + ownerType
                            )
                    );
        }
        List<InventarioSucursal> inventarios =
                inventarioSucursalRepository.findByProduct_IdAndBranch_Id(
                        producto.getId(),
                        sucursal.getId()
                );

        if (inventarios.isEmpty()) {
            throw new NotFoundException(
                    "Inventario no encontrado para el producto " + producto.getName()
            );
        }
        if (inventarios.size() > 1) {
            throw new IllegalStateException(
                    "Configuración inválida: existen múltiples inventarios "
                            + "para el producto " + producto.getName()
                            + " en una sucursal sin inventario por dueño"
            );
        }

        return inventarios.get(0);
    }

    private void registrarMovimiento(
            InventarioSucursal inv,
            BigDecimal qty,
            BigDecimal before,
            BigDecimal after,
            Long compraId
    ) {
        HistorialMovimiento mov = new HistorialMovimiento();
        mov.setInventarioSucursal(inv);
        mov.setQuantity(qty);
        mov.setMovementType(TipoMovimiento.SALIDA);
        mov.setReference("Devolución de COMPRA #" + compraId);
        mov.setMovementDate(LocalDateTime.now());
        mov.setBeforeStock(before);
        mov.setNewStock(after);
        historialMovimientoRepository.save(mov);
    }

}
