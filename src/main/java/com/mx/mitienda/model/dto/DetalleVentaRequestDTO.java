package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaRequestDTO {
    Long productId;
    private BigDecimal quantity;
    private InventarioOwnerType ownerType;
}
