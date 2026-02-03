package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

@Data
public class InventarioSucursalRequestDTO {
    private Long productId;
    private Long branchId;
    private Integer quantity;
    private Integer minStock;
    private Integer maxStock;
    private Boolean isStockCritico;
    private InventarioOwnerType ownerType;
}
