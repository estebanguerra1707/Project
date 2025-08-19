package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductoDTO {
    private String name;
    private String sku;
    private Long categoryId; // ← ahora usamos ID
    private String description;
    private BigDecimal purchasePrice;
    private Integer stock;
    private Long providerId;
    private LocalDateTime updatedDate;
    private String codigoBarras;
}
