package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MovimientoStockDTO {
    private Long productId;
    private BigDecimal cantidad;
    private InventarioOwnerType ownerType;

}
