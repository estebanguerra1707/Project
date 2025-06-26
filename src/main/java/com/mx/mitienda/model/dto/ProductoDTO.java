package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductoDTO {
    private String name;
    private String sku;
    private Long categoryId; // ← ahora usamos ID
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long businessTypeId;
    private Long providerId;
    private LocalDate updatedDate;
}
