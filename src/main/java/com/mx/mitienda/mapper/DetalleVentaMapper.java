package com.mx.mitienda.mapper;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DetalleVentaMapper {
    public DetalleVentaResponseDTO toResponse(DetalleVenta detalleVenta){
        DetalleVentaResponseDTO detalleVentaResponseDTO = new DetalleVentaResponseDTO();
        detalleVentaResponseDTO.setProductName(detalleVenta.getProduct().getName());
        detalleVentaResponseDTO.setQuantity(detalleVenta.getQuantity());
        detalleVentaResponseDTO.setUnitPrice(detalleVenta.getUnitPrice());
        detalleVentaResponseDTO.setSubTotal(detalleVenta.getSubTotal());
        detalleVentaResponseDTO.setInventarioOwnerType(detalleVenta.getOwnerType());
        return detalleVentaResponseDTO;
    }

}
