package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleDevolucionCompraDTO {
    private Long productId;
    private String productName;
    private Integer cantidadDevuelta;
    private BigDecimal precioCompra;
}
