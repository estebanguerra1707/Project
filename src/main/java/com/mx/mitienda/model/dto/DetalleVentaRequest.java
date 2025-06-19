package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaRequest {
    private Long productId;
    private int quantity;
    private BigDecimal price;

}
