package com.mx.mitienda.mapper;

import com.mx.mitienda.model.DevolucionVentas;
import com.mx.mitienda.model.dto.FiltroDevolucionVentasResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FiltroDevolucionVentasMapper {
    public FiltroDevolucionVentasResponseDTO toDto(DevolucionVentas entity) {
        FiltroDevolucionVentasResponseDTO dto = new FiltroDevolucionVentasResponseDTO();

        dto.setId(entity.getId());
        dto.setVentaId(entity.getVenta().getId());
        dto.setFechaDevolucion(entity.getFechaDevolucion());
        dto.setMotivo(entity.getMotivo());

        dto.setMontoDevuelto(entity.getMontoDevuelto());
        dto.setTipoDevolucion(entity.getTipoDevolucion().name());

        dto.setUsername(entity.getUsuario().getEmail());       // o .getUsername()
        dto.setBranchName(entity.getBranch().getName());

        // Primer detalle o suma si hay varios
        var det = entity.getDetalles().get(0);
        dto.setProductName(det.getProducto().getName());
        dto.setProductCode(det.getProducto().getCodigoBarras());
        dto.setCantidadDevuelta(det.getCantidadDevuelta());

        return dto;
    }
}
