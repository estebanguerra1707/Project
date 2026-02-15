package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventarioSucursalRequestDTO {
    private Long productId;
    private Long branchId;
    private BigDecimal quantity;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private Boolean isStockCritico;
    private InventarioOwnerType ownerType;
}
