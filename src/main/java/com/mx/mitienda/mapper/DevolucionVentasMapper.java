package com.mx.mitienda.mapper;

import com.mx.mitienda.model.DetalleDevolucionVentas;
import com.mx.mitienda.model.DevolucionVentas;
import com.mx.mitienda.model.dto.DetalleDevolucionVentaDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DevolucionVentasMapper {
    public DevolucionVentasResponseDTO toResponse(DevolucionVentas devolucion) {
        DevolucionVentasResponseDTO devolucionVentasResponseDTO = new DevolucionVentasResponseDTO();
        devolucionVentasResponseDTO.setId(devolucion.getId());
        devolucionVentasResponseDTO.setFechaDevolucion(devolucion.getFechaDevolucion());
        devolucionVentasResponseDTO.setMotivo(devolucion.getMotivo());
        devolucionVentasResponseDTO.setVentaId(devolucion.getVenta().getId());
        devolucionVentasResponseDTO.setUsuario(devolucion.getUsuario().getUsername());
        devolucionVentasResponseDTO.setSucursal(devolucion.getBranch().getName());
        devolucionVentasResponseDTO.setTipoDevolucion(devolucion.getTipoDevolucion().name());
        devolucionVentasResponseDTO.setTotalDevolucion(devolucion.getMontoDevuelto());
        List<DetalleDevolucionVentaDTO> detalles = devolucion.getDetalles()
                .stream()
                .map(this::toDetalleResponse)
                .collect(Collectors.toList());

        devolucionVentasResponseDTO.setDetalles(detalles);
        return devolucionVentasResponseDTO;
    }


    private DetalleDevolucionVentaDTO toDetalleResponse(DetalleDevolucionVentas detalle) {
        DetalleDevolucionVentaDTO detalleDevolucionVentaDTO = new DetalleDevolucionVentaDTO();
        detalleDevolucionVentaDTO.setProductId(detalle.getProducto().getId());
        detalleDevolucionVentaDTO.setProductName(detalle.getProducto().getName());
        detalleDevolucionVentaDTO.setCantidadDevuelta(detalle.getCantidadDevuelta());
        detalleDevolucionVentaDTO.setPrecioUnitario(detalle.getPrecioUnitario());
        detalleDevolucionVentaDTO.setInventarioOwnerType(detalle.getDetalleVenta().getOwnerType());
        return detalleDevolucionVentaDTO;
    }
}
