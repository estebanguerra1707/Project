package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DevolucionVentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import com.mx.mitienda.repository.*;

import com.mx.mitienda.util.enums.TipoDevolucion;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DevolucionVentasServiceImpl implements IDevolucionVentasService{


    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleDevolucionVentasRepository detalleDevolucionVentasRepository;
    private final DevolucionVentasRepository devolucionVentasRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final DevolucionVentasMapper devolucionMapper;
    private final HistorialMovimientoRepository historialMovimientoRepository;

    @Override
    @Transactional
    public DevolucionVentasResponseDTO procesarDevolucion(DevolucionVentasRequestDTO devolucionVentasRequestDTO, Authentication auth) {

        Usuario usuario = authenticatedUserService.getCurrentUser();
        Long branchId = authenticatedUserService.getCurrentBranchId();
        Long bussinessId = authenticatedUserService.getCurrentBusinessTypeId();

        //verificar la venta
        Venta venta = ventaRepository.findByIdAndBranch_IdAndActiveTrue(devolucionVentasRequestDTO.getVentaId(), branchId)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada en tu sucursal"));

        //buscar el producto por el codigo de barras
        Producto producto = productoRepository.findByCodigoBarrasAndBusinessTypeId(devolucionVentasRequestDTO.getCodigoBarras(), bussinessId)
                .orElseThrow(()-> new NotFoundException("El producto no ha sido encontrado, favor de validar:: " + devolucionVentasRequestDTO.getCodigoBarras()));

        //verificar que el producto este en venta, match entre producto y el detalle de la venta
        DetalleVenta detalleVenta = venta.getDetailsList().stream().filter((d-> d.getProduct().getId().equals(producto.getId()))).findFirst().orElseThrow(()-> new NotFoundException("El producto no forma parte de la venta seleccionada, intente de nuevo."));

        //validar la cantidad devuelta
        int cantidadVendida = detalleVenta.getQuantity();
        int cantidadDevuelta = Optional.ofNullable(
                detalleDevolucionVentasRepository.sumCantidadDevueltaPorVentaYProducto(venta.getId(), producto.getId())
        ).orElse(0);
        int cantidadRestante = cantidadVendida - cantidadDevuelta;

        if(devolucionVentasRequestDTO.getCantidad() > cantidadRestante){
            throw new IllegalArgumentException("No puedes devolver más de lo vendido");
        }

        //crear el detalle de la devolucion

        DevolucionVentas devolucionVentas = new DevolucionVentas();
        devolucionVentas.setVenta(venta);
        devolucionVentas.setBranch(authenticatedUserService.getCurrentBranch());
        devolucionVentas.setUsuario(usuario);
        devolucionVentas.setMotivo(devolucionVentasRequestDTO.getMotivo());
        devolucionVentas.setFechaDevolucion(LocalDateTime.now());
        devolucionVentas.setTipoDevolucion(devolucionVentasRequestDTO.getCantidad().equals(cantidadVendida)? TipoDevolucion.TOTAL: TipoDevolucion.PARCIAL);

        DetalleDevolucionVentas detalleDevolucionVentas = new DetalleDevolucionVentas();
        detalleDevolucionVentas.setProducto(producto);
        detalleDevolucionVentas.setDetalleVenta(detalleVenta);
        detalleDevolucionVentas.setCantidadDevuelta(devolucionVentasRequestDTO.getCantidad());
        detalleDevolucionVentas.setPrecioUnitario(detalleVenta.getUnitPrice());
        detalleDevolucionVentas.setDevolucion(devolucionVentas);
        BigDecimal montoDevuelto = detalleVenta.getUnitPrice()
                .multiply(BigDecimal.valueOf(devolucionVentasRequestDTO.getCantidad()));

        devolucionVentas.setMontoDevuelto(montoDevuelto);
        devolucionVentas.setDetalles(List.of(detalleDevolucionVentas));
        devolucionVentasRepository.save(devolucionVentas);

        //actualizar el inventario

        InventarioSucursal inventarioSucursal = inventarioSucursalRepository.findByProduct_IdAndBranch_Id(producto.getId(), branchId)
                .orElseThrow(() -> new NotFoundException("Inventario no encontrado"));
        inventarioSucursal.setStock(inventarioSucursal.getStock() + devolucionVentasRequestDTO.getCantidad());
        inventarioSucursalRepository.save(inventarioSucursal);


        //ajustar ganacias o historial de movimientos

// ajustar ganancias
        BigDecimal precioVenta = detalleVenta.getUnitPrice();
        BigDecimal precioCompra = producto.getPurchasePrice(); // o buscar desde compras
        BigDecimal gananciaPerdida = precioVenta.subtract(precioCompra)
                .multiply(BigDecimal.valueOf(cantidadDevuelta));
// aquí podrías guardar esta pérdida en una tabla resumen si aplica

// historial de movimientos
        int stockAnterior = inventarioSucursal.getStock() - devolucionVentasRequestDTO.getCantidad();
        int stockNuevo = inventarioSucursal.getStock(); // ya actualizado arriba
        HistorialMovimiento movimiento = new HistorialMovimiento();
        movimiento.setInventarioSucursal(inventarioSucursal);
        movimiento.setQuantity(cantidadDevuelta);
        movimiento.setMovementType(TipoMovimiento.ENTRADA);
        movimiento.setReference("Devolución de venta #" + venta.getId());
        movimiento.setMovementDate(LocalDateTime.now());
        movimiento.setBeforeStock(stockAnterior);
        movimiento.setNewStock(stockNuevo);
        historialMovimientoRepository.save(movimiento);

        return devolucionMapper.toResponse(devolucionVentas);
    }

}
