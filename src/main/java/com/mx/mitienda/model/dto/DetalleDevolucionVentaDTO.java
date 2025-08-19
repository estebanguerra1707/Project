package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleDevolucionVentaDTO {
    private Long productId;
    private String productName;
    private Integer cantidadDevuelta;
    private BigDecimal precioUnitario;
}
