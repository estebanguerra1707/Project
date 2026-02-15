package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DevolucionVentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.util.enums.TipoDevolucion;
import com.mx.mitienda.util.enums.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevolucionVentasServiceImplTest {

    @InjectMocks
    private DevolucionVentasServiceImpl service;

    @Mock private VentaRepository ventaRepo;
    @Mock private ProductoRepository productoRepo;
    @Mock private DetalleDevolucionVentasRepository detalleDevolucionRepo;
    @Mock private DevolucionVentasRepository devolucionRepo;
    @Mock private InventarioSucursalRepository inventarioRepo;
    @Mock private AuthenticatedUserServiceImpl authService;
    @Mock private DevolucionVentasMapper mapper;
    @Mock private HistorialMovimientoRepository historialRepo;
    @Mock
    private DevolucionVentasMapper devolucionMapper;

    private Venta venta;
    private Producto producto;
    private DetalleVenta detalleVenta;
    private InventarioSucursal inventario;

    private Usuario usuario;

    private DevolucionVentasRequestDTO req;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        req = new DevolucionVentasRequestDTO();
        req.setVentaId(10L);
        req.setCodigoBarras("COD123");
        req.setCantidad(BigDecimal.valueOf(2));
        req.setMotivo("Prueba");

        usuario = new Usuario();
        usuario.setId(1L);

        producto = new Producto();
        producto.setId(5L);
        producto.setPurchasePrice(new BigDecimal("10.00"));

        detalleVenta = new DetalleVenta();
        detalleVenta.setProduct(producto);
        detalleVenta.setQuantity(BigDecimal.valueOf(5));
        detalleVenta.setUnitPrice(new BigDecimal("20.00"));

        venta = new Venta();
        venta.setId(10L);
        venta.setDetailsList(java.util.List.of(detalleVenta));

        inventario = new InventarioSucursal();
        inventario.setStock(BigDecimal.valueOf(20));
        inventario.setProduct(producto);
    }

    // ───────────────────────────────────────────────
    @Test
    void venta_no_encontrada_usuario_normal() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(false, 1L, 1L, "x", false, null));

        when(ventaRepo.findByIdAndBranch_IdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.procesarDevolucion(req));
    }

    // ───────────────────────────────────────────────
    @Test
    void venta_no_encontrada_superadmin() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(true, null, null, "x", false, null));

        when(ventaRepo.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.procesarDevolucion(req));
    }

    // ───────────────────────────────────────────────
    @Test
    void producto_no_encontrado() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser())
                .thenReturn(usuario);

        when(ventaRepo.findByIdAndBranch_IdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(venta));

        when(productoRepo.findByCodigoBarrasAndBusinessTypeId("COD123", 1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.procesarDevolucion(req));
    }

    // ───────────────────────────────────────────────
    @Test
    void producto_no_pertenece_a_la_venta() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser())
                .thenReturn(usuario);

        when(ventaRepo.findByIdAndBranch_IdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(venta));

        Producto otroProducto = new Producto();
        otroProducto.setId(99L);

        when(productoRepo.findByCodigoBarrasAndBusinessTypeId("COD123", 1L))
                .thenReturn(Optional.of(otroProducto));

        assertThrows(NotFoundException.class, () -> service.procesarDevolucion(req));
    }

    // ───────────────────────────────────────────────
    @Test
    void devolucion_mayor_a_lo_permitido() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser())
                .thenReturn(usuario);

        when(ventaRepo.findByIdAndBranch_IdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(venta));
        when(productoRepo.findByCodigoBarrasAndBusinessTypeId("COD123", 1L))
                .thenReturn(Optional.of(producto));

        when(detalleDevolucionRepo.sumCantidadDevueltaPorVentaYProducto(10L, 5L))
                .thenReturn(BigDecimal.valueOf(4));

        req.setCantidad(BigDecimal.valueOf(3));

        assertThrows(IllegalArgumentException.class, () -> service.procesarDevolucion(req));
    }

    // ───────────────────────────────────────────────
    @Test
    void devolucion_parcial_exitosa() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser())
                .thenReturn(usuario);

        when(ventaRepo.findByIdAndBranch_IdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(venta));
        when(productoRepo.findByCodigoBarrasAndBusinessTypeId("COD123", 1L))
                .thenReturn(Optional.of(producto));

        when(detalleDevolucionRepo.sumCantidadDevueltaPorVentaYProducto(10L, 5L))
                .thenReturn(BigDecimal.ONE);

        when(inventarioRepo.findByProduct_IdAndBranch_Id(5L, 1L))
                .thenReturn(List.of(inventario));

        DevolucionVentas devolucion = new DevolucionVentas();
        devolucion.setDetalles(java.util.List.of(new DetalleDevolucionVentas()));
        when(devolucionRepo.save(any())).thenReturn(devolucion);

        when(devolucionMapper.toResponse(any())).thenReturn(new DevolucionVentasResponseDTO());

        var resp = service.procesarDevolucion(req);

        assertNotNull(resp);
        assertEquals(TipoDevolucion.PARCIAL, devolucion.getTipoDevolucion());
        verify(historialRepo).save(any());
        verify(inventarioRepo).save(any());
    }

    // ───────────────────────────────────────────────
    @Test
    void devolucion_total_exitosa() {
        when(authService.getUserContext())
                .thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser())
                .thenReturn(usuario);

        when(ventaRepo.findByIdAndBranch_IdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(venta));
        when(productoRepo.findByCodigoBarrasAndBusinessTypeId("COD123", 1L))
                .thenReturn(Optional.of(producto));

        when(detalleDevolucionRepo.sumCantidadDevueltaPorVentaYProducto(10L, 5L))
                .thenReturn(BigDecimal.valueOf(3));

        req.setCantidad(BigDecimal.valueOf(2));

        when(inventarioRepo.findByProduct_IdAndBranch_Id(5L, 1L))
                .thenReturn(List.of(inventario));

        DevolucionVentas devolucion = new DevolucionVentas();
        devolucion.setDetalles(java.util.List.of(new DetalleDevolucionVentas()));
        when(devolucionRepo.save(any())).thenReturn(devolucion);

        when(devolucionMapper.toResponse(any())).thenReturn(new DevolucionVentasResponseDTO());

        var resp = service.procesarDevolucion(req);

        assertEquals(TipoDevolucion.TOTAL, devolucion.getTipoDevolucion());
    }
}
