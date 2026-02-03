package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

@Data
public class MovimientoStockDTO {
    private Long productId;
    private Integer cantidad;
    private InventarioOwnerType ownerType;

}
