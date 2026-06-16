package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VentaPagoRequestDTO {
    private BigDecimal amount;
    private Long paymentMethodId;
    private String note;
}