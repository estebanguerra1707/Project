package com.mx.mitienda.mapper;

import com.mx.mitienda.model.dto.ProductoInventarioSucursalDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductoInventarioMapper {
    public ProductoInventarioSucursalDTO fromRow(Object[] row) {
        ProductoInventarioSucursalDTO dto = new ProductoInventarioSucursalDTO();
        dto.setProductoId((Long) row[0]);
        dto.setProductoNombre((String) row[1]);
        dto.setSucursalId((Long) row[2]);
        dto.setSucursalNombre((String) row[3]);
        dto.setStock((Integer) row[4]);
        return dto;
    }
}
