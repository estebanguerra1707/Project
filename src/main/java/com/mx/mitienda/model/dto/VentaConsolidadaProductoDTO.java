package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VentaConsolidadaProductoDTO {

    private Long productId;

    private String productName;

    private String unitAbbr;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal subTotal;
}