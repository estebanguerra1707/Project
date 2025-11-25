package com.mx.mitienda.mapper;

import com.mx.mitienda.model.DetalleDevolucionCompras;
import com.mx.mitienda.model.DevolucionCompras;
import com.mx.mitienda.model.dto.FiltroDevolucionComprasResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FiltroDevolucionComprasMapper {
    public FiltroDevolucionComprasResponseDTO toDto(DevolucionCompras entity) {

        DetalleDevolucionCompras detalle = entity.getDetalles().isEmpty()
                ? null
                : entity.getDetalles().get(0);

        return FiltroDevolucionComprasResponseDTO.builder()
                .id(entity.getId())
                .compraId(entity.getCompra().getId())
                .fechaDevolucion(entity.getFecha())
                .montoDevuelto(entity.getMontoDevuelto())
                .tipoDevolucion(entity.getTipoDevolucion().name())

                .productName(detalle != null ? detalle.getProducto().getName() : null)
                .codigoBarras(detalle != null ? detalle.getProducto().getCodigoBarras() : null)
                .cantidadDevuelta(detalle != null ? detalle.getCantidadDevuelta() : null)

                .username(entity.getUsuario().getUsername())
                .branchName(entity.getBranch().getName())
                .build();
    }
}
