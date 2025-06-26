package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoResponseDTO {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer stock_quantity;
    private String categoryName;
    private String providerName;
    private String bussinessTypeName;
}
