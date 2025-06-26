package com.mx.mitienda.mapper;

import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProductoMapper {
    public Producto toEntity(ProductoDTO dto, ProductCategory category, Proveedor proveedor, BusinessType businessType) {
        Producto producto= new Producto();
        producto.setName(dto.getName());
        producto.setSku(dto.getSku());
        producto.setDescription(dto.getDescription());
        producto.setPrice(dto.getPrice());
        producto.setStock_quantity(dto.getStock());
        producto.setActive(true);
        producto.setCreation_date(LocalDate.now());

        producto.setProductCategory(category);
        producto.setProvider(proveedor);
        producto.setBusinessType(businessType);

        return producto;
    }

    public ProductoResponseDTO toResponse(Producto producto){
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setName(producto.getName());
        dto.setSku(producto.getSku());
        dto.setDescription(producto.getDescription());
        dto.setPrice(producto.getPrice());
        dto.setStock_quantity(producto.getStock_quantity());

        if (producto.getProductCategory() != null)
            dto.setCategoryName(producto.getProductCategory().getName());

        if (producto.getProvider() != null)
            dto.setProviderName(producto.getProvider().getName());

        if (producto.getBusinessType() != null)
            dto.setBussinessTypeName(producto.getBusinessType().getName());

        return dto;
    }
}
