package com.mx.mitienda.service;


import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DevolucionComprasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DevolucionComprasRequestDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.util.enums.TipoDevolucion;
import com.mx.mitienda.util.enums.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevolucionComprasServiceImplTest {

    @InjectMocks
    private DevolucionComprasServiceImpl service;

    @Mock private ProductoRepository productoRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private DetalleDevolucionComprasRepository detalleDevolucionComprasRepository;
    @Mock private InventarioSucursalRepository inventarioRepository;
    @Mock private AuthenticatedUserServiceImpl authService;
    @Mock private HistorialMovimientoRepository historialRepository;
    @Mock private DevolucionComprasMapper mapper;
    @Mock private DevolucionComprasRepository devolucionRepository;

    private DevolucionComprasRequestDTO request;
    private Compra compra;
    private Producto producto;
    private DetalleCompra detalle;
    private InventarioSucursal inventario;
    private Usuario usuario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        request = new DevolucionComprasRequestDTO();
        request.setCompraId(1L);
        request.setCodigoBarras("ABC123");
        request.setCantidad(2);

        usuario = new Usuario();
        usuario.setId(10L);

        producto = new Producto();
        producto.setId(5L);
        producto.setCodigoBarras("ABC123");

        detalle = new DetalleCompra();
        detalle.setProduct(producto);
        detalle.setQuantity(10);

        compra = new Compra();
        compra.setId(1L);
        compra.setDetails(java.util.List.of(detalle));

        inventario = new InventarioSucursal();
        inventario.setProduct(producto);
        inventario.setStock(20);
    }

    // ───────────────────────────────────────────────
    @Test
    void error_cuando_compra_no_existe() {
        when(authService.getUserContext()).thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(compraRepository.findByIdAndBranch_IdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.procesarDevolucion(request));
    }

    // ───────────────────────────────────────────────
    @Test
    void error_cuando_producto_no_existe() {
        when(authService.getUserContext()).thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(compraRepository.findByIdAndBranch_IdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(compra));
        when(productoRepository.findByCodigoBarrasAndBusinessTypeId("ABC123", 1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.procesarDevolucion(request));
    }

    // ───────────────────────────────────────────────
    @Test
    void error_cuando_se_devuelve_mas_de_lo_comprado() {
        when(authService.getUserContext()).thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser()).thenReturn(usuario);

        when(compraRepository.findByIdAndBranch_IdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(compra));

        when(productoRepository.findByCodigoBarrasAndBusinessTypeId("ABC123", 1L))
                .thenReturn(Optional.of(producto));

        when(detalleDevolucionComprasRepository.sumCantidadDevueltaPorCompraYProducto(1L, 5L))
                .thenReturn(9); // ya se devolvieron 9

        request.setCantidad(5); // quiere devolver 5 → supera lo permitido (solo queda 1)

        assertThrows(IllegalArgumentException.class, () -> service.procesarDevolucion(request));
    }

    // ───────────────────────────────────────────────
    @Test
    void devolucion_parcial_exitosa() {
        when(authService.getUserContext()).thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser()).thenReturn(usuario);

        when(compraRepository.findByIdAndBranch_IdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(compra));
        when(productoRepository.findByCodigoBarrasAndBusinessTypeId("ABC123", 1L))
                .thenReturn(Optional.of(producto));

        when(detalleDevolucionComprasRepository.sumCantidadDevueltaPorCompraYProducto(1L, 5L))
                .thenReturn(0); // nada devuelto antes

        when(inventarioRepository.findByProduct_IdAndBranch_Id(5L, 1L))
                .thenReturn(Optional.of(inventario));

        DevolucionCompras devolucion = new DevolucionCompras();
        devolucion.setDetalles(java.util.List.of(new DetalleDevolucionCompras()));

        when(mapper.toEntity(any(), any(), any(), any(), any())).thenReturn(devolucion);

        var response = service.procesarDevolucion(request);

        assertNotNull(response);
        assertEquals(TipoDevolucion.PARCIAL, devolucion.getTipoDevolucion());
        verify(inventarioRepository).save(any());
        verify(historialRepository).save(any());
    }

    // ───────────────────────────────────────────────
    @Test
    void devolucion_total_exitosa() {
        when(authService.getUserContext()).thenReturn(new UserContext(false, 1L, 1L, "x", false, null));
        when(authService.getCurrentUser()).thenReturn(usuario);

        when(compraRepository.findByIdAndBranch_IdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(compra));
        when(productoRepository.findByCodigoBarrasAndBusinessTypeId("ABC123", 1L))
                .thenReturn(Optional.of(producto));

        when(detalleDevolucionComprasRepository.sumCantidadDevueltaPorCompraYProducto(1L, 5L))
                .thenReturn(8); // ya devolvieron 8

        request.setCantidad(2); // 8 + 2 = 10 (cantidad comprada)

        when(inventarioRepository.findByProduct_IdAndBranch_Id(5L, 1L))
                .thenReturn(Optional.of(inventario));

        DevolucionCompras devolucion = new DevolucionCompras();
        devolucion.setDetalles(java.util.List.of(new DetalleDevolucionCompras()));

        when(mapper.toEntity(any(), any(), any(), any(), any())).thenReturn(devolucion);

        var response = service.procesarDevolucion(request);

        assertEquals(TipoDevolucion.TOTAL, devolucion.getTipoDevolucion());
    }
}
