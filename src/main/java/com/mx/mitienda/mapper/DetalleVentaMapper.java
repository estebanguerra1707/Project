package com.mx.mitienda.mapper;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.UnidadMedidaEntity;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DetalleVentaMapper {
    public DetalleVentaResponseDTO toResponse(DetalleVenta detail) {
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();

        Producto p = detail.getProduct();
        dto.setProductId(p != null ? p.getId() : null);
        dto.setProductName(p != null ? p.getName() : null);
        dto.setSku(p != null ? p.getSku() : null);
        dto.setCodigoBarras(p != null ? p.getCodigoBarras() : null);

        if (p != null && p.getBranch() != null) {
            dto.setBranchId(p.getBranch().getId());
            dto.setBranchName(p.getBranch().getName());
        }

        if (p != null && p.getBusinessType() != null) {
            dto.setBusinessTypeId(p.getBusinessType().getId());
            dto.setBusinessTypeName(p.getBusinessType().getName());
        }

        dto.setQuantity(detail.getQuantity());
        dto.setUnitPrice(detail.getUnitPrice());
        dto.setSubTotal(detail.getSubTotal());

        UnidadMedidaEntity um = (p != null) ? p.getUnidadMedida() : null;
        if (um != null) {
            dto.setUnitId(um.getId());
            dto.setUnitAbbr(um.getAbreviatura());
            dto.setUnitName(um.getNombre());
            dto.setPermiteDecimales(um.isPermiteDecimales());
        } else {
            dto.setUnitId(null);
            dto.setUnitAbbr(null);
            dto.setUnitName(null);
            dto.setPermiteDecimales(p != null && p.isPermiteDecimales());
        }

        dto.setInventarioOwnerType(detail.getOwnerType());

        // usaInventarioPorDuenio viene de la sucursal de la venta
        if (detail.getVenta() != null && detail.getVenta().getBranch() != null) {
            dto.setUsaInventarioPorDuenio(detail.getVenta().getBranch().getUsaInventarioPorDuenio());
        } else {
            dto.setUsaInventarioPorDuenio(null);
        }

        return dto;
    }

}
