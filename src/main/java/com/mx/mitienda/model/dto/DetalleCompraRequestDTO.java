package com.mx.mitienda.model.dto;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleCompraRequestDTO {
    private Long productId;
    private BigDecimal quantity;
    private InventarioOwnerType ownerType;
}

