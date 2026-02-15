package com.mx.mitienda.service;

import com.mx.mitienda.exception.BadRequestException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DevolucionVentasMapper;
import com.mx.mitienda.mapper.FiltroDevolucionVentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import com.mx.mitienda.model.dto.DevolucionesVentasFiltroDTO;
import com.mx.mitienda.model.dto.FiltroDevolucionVentasResponseDTO;
import com.mx.mitienda.repository.*;

import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.DevolucionVentasSpecBuilder;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.util.enums.TipoDevolucion;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.mx.mitienda.util.enums.TipoDevolucion.PARCIAL;
import static com.mx.mitienda.util.enums.TipoDevolucion.TOTAL;

@Service
public class DevolucionVentasServiceImpl extends BaseService implements IDevolucionVentasService{


    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleDevolucionVentasRepository detalleDevolucionVentasRepository;
    private final DevolucionVentasRepository devolucionVentasRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final DevolucionVentasMapper devolucionMapper;
    private final FiltroDevolucionVentasMapper filtroDevolucionVentasMapper;
    private final HistorialMovimientoRepository historialMovimientoRepository;
    private final SucursalRepository sucursalRepository;

    public DevolucionVentasServiceImpl(VentaRepository ventaRepository, ProductoRepository productoRepository,
                                       DetalleDevolucionVentasRepository detalleDevolucionVentasRepository,
                                       DevolucionVentasRepository devolucionVentasRepository,
                                       InventarioSucursalRepository inventarioSucursalRepository,
                                       AuthenticatedUserServiceImpl authenticatedUserService,
                                       DevolucionVentasMapper devolucionMapper,
                                       HistorialMovimientoRepository historialMovimientoRepository,
                                       SucursalRepository sucursalRepository,
                                        FiltroDevolucionVentasMapper filtroDevolucionVentasMapper) {
        super(authenticatedUserService);
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.detalleDevolucionVentasRepository = detalleDevolucionVentasRepository;
        this.devolucionVentasRepository = devolucionVentasRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.devolucionMapper = devolucionMapper;
        this.historialMovimientoRepository = historialMovimientoRepository;
        this.sucursalRepository = sucursalRepository;
        this.filtroDevolucionVentasMapper = filtroDevolucionVentasMapper;
    }

    @Override
    @Transactional
    public DevolucionVentasResponseDTO procesarDevolucion(DevolucionVentasRequestDTO devolucionVentasRequestDTO) {

        UserContext ctx = ctx();
        Long branchId = ctx.isSuperAdmin()
                ? devolucionVentasRequestDTO.getBranchId()
                : ctx.getBranchId();

        Long businessId =  ctx.isSuperAdmin()
                ? devolucionVentasRequestDTO.getBusinessTypeId()
                : ctx.getBusinessTypeId();

        Usuario usuario = authenticatedUserService.getCurrentUser();

        Venta venta = obtenerVentaValida(devolucionVentasRequestDTO.getVentaId(), branchId);

        //buscar el producto por el codigo de barras
        Producto producto = obtenerProductoValido(devolucionVentasRequestDTO.getCodigoBarras(), businessId);

        //verificar que el producto este en venta, match entre producto y el detalle de la venta
        DetalleVenta detalleVenta = obtenerDetalleVenta(venta, producto.getId());

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(()-> new RuntimeException("La sucursal no ha sido encontrada"));

        //validar la cantidad devuelta
        BigDecimal cantidadVendida = detalleVenta.getQuantity(); // BigDecimal
        BigDecimal cantidadDevueltaAcum = Optional.ofNullable(
                detalleDevolucionVentasRepository.sumCantidadDevueltaPorVentaYProducto(
                        venta.getId(), producto.getId()
                )
        ).orElse(BigDecimal.ZERO);

        BigDecimal cantidadRestante = cantidadVendida.subtract(cantidadDevueltaAcum);
        BigDecimal cantidadSolicitada = devolucionVentasRequestDTO.getCantidad(); // BigDecimal

        if (cantidadSolicitada == null || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La cantidad a devolver debe ser mayor a 0");
        }

        if (cantidadSolicitada.compareTo(cantidadRestante) > 0) {
            throw new IllegalArgumentException("No puedes devolver más de lo vendido");
        }


// CREAR DEVOLUCIÓN
        DevolucionVentas devolucionVentas = new DevolucionVentas();
        devolucionVentas.setVenta(venta);
        devolucionVentas.setBranch(sucursal);
        devolucionVentas.setUsuario(usuario);
        devolucionVentas.setMotivo(devolucionVentasRequestDTO.getMotivo());
        devolucionVentas.setFechaDevolucion(LocalDateTime.now());

        BigDecimal totalDevuelto = cantidadDevueltaAcum.add(cantidadSolicitada);

        boolean esTotal = totalDevuelto.compareTo(cantidadVendida) >= 0;
        devolucionVentas.setTipoDevolucion(esTotal ? TOTAL : PARCIAL);

// DETALLE
        DetalleDevolucionVentas detalleDevolucionVentas = new DetalleDevolucionVentas();
        detalleDevolucionVentas.setProducto(producto);
        detalleDevolucionVentas.setDetalleVenta(detalleVenta);
        detalleDevolucionVentas.setCantidadDevuelta(cantidadSolicitada);
        detalleDevolucionVentas.setPrecioUnitario(detalleVenta.getUnitPrice());
        detalleDevolucionVentas.setDevolucion(devolucionVentas);

// monto devuelto
        BigDecimal montoDevuelto = detalleVenta.getUnitPrice().multiply(cantidadSolicitada);
        devolucionVentas.setMontoDevuelto(montoDevuelto);

        devolucionVentas.setDetalles(List.of(detalleDevolucionVentas));
        devolucionVentasRepository.save(devolucionVentas);

// actualizar inventario
        InventarioSucursal inventarioSucursal =
                obtenerInventario(producto, sucursal, detalleVenta);

        BigDecimal stockAnterior = inventarioSucursal.getStock(); // BigDecimal
        BigDecimal stockNuevo = stockAnterior.add(cantidadSolicitada);

        inventarioSucursal.setStock(stockNuevo);
        inventarioSucursalRepository.save(inventarioSucursal);

// historial
        HistorialMovimiento movimiento = new HistorialMovimiento();
        movimiento.setInventarioSucursal(inventarioSucursal);
        movimiento.setQuantity(cantidadSolicitada);
        movimiento.setMovementType(TipoMovimiento.ENTRADA);
        movimiento.setReference("Devolución de venta #" + venta.getId());
        movimiento.setMovementDate(LocalDateTime.now());
        movimiento.setBeforeStock(stockAnterior);
        movimiento.setNewStock(stockNuevo);
        historialMovimientoRepository.save(movimiento);

        return devolucionMapper.toResponse(devolucionVentas);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FiltroDevolucionVentasResponseDTO> findByFilter(DevolucionesVentasFiltroDTO filterDTO,
                                                                int page,
                                                                int size) {
        Long branchId = ctx().isSuperAdmin()
                ? filterDTO.getBranchId()
                : ctx().getBranchId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        DevolucionVentasSpecBuilder builder = new DevolucionVentasSpecBuilder()
                .withId(filterDTO.getId())
                .ventaId(filterDTO.getVentaId())
                .username(filterDTO.getUsername())
                .dateBetween(filterDTO.getStartDate(), filterDTO.getEndDate())
                .perDayMonthYear(filterDTO.getDay(), filterDTO.getMonth(), filterDTO.getYear())
                .montoBetween(filterDTO.getMinMonto(), filterDTO.getMaxMonto())
                .cantidadBetween(filterDTO.getMinCantidad(), filterDTO.getMaxCantidad())
                .tipoDevolucion(filterDTO.getTipoDevolucion())
                .codigoBarras(filterDTO.getCodigoBarras())
                .productName(filterDTO.getProductName());

        Specification<DevolucionVentas> spec = builder.build();

        if (!ctx().isSuperAdmin()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("branch").get("id"), branchId));
        }
        return devolucionVentasRepository.findAll(spec, pageable)
                .map(filtroDevolucionVentasMapper::toDto);
    }

    private Venta obtenerVentaValida(Long ventaId, Long branchId) {
        return ventaRepository.findByIdAndBranch_IdAndActiveTrue(ventaId, branchId)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada en tu sucursal"));
    }


    private Producto obtenerProductoValido(String codigo, Long businessId) {
       return  productoRepository.findByCodigoBarrasAndBusinessTypeId(codigo, businessId)
                .orElseThrow(()-> new NotFoundException("El producto no ha sido encontrado, favor de validar:: " + codigo));
    }

    private DetalleVenta obtenerDetalleVenta(Venta venta, Long productId) {
       return  venta.getDetailsList().stream().filter((
               d-> d.getProduct().getId().equals(productId))).findFirst()
               .orElseThrow(()-> new NotFoundException("El producto no forma parte de la venta seleccionada, intente de nuevo."));
    }



    private InventarioSucursal obtenerInventario(
            Producto producto,
            Sucursal sucursal,
            DetalleVenta detalleVenta
    ) {

        boolean usaInventarioPorDuenio =
                Boolean.TRUE.equals(sucursal.getUsaInventarioPorDuenio());

        if (usaInventarioPorDuenio) {

            InventarioOwnerType ownerType =
                    detalleVenta.getOwnerType() != null
                            ? detalleVenta.getOwnerType()
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

        return inventarios.get(0);

    }

}
