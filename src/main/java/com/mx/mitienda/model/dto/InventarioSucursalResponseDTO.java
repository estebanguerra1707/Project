package com.mx.mitienda.model.dto;

import lombok.Data;
import lombok.Locked;

import java.time.LocalDateTime;

@Data
public class InventarioSucursalResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long branchId;
    private String branchName;
    private String sku;
    private Integer stock;
    private Integer minStock;
    private Integer maxStock;
    private LocalDateTime lastUpdated;
    private String updatedBy;
}