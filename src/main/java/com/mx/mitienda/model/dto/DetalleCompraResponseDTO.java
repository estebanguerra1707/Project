package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleCompraResponseDTO {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
