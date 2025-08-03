package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ReporteGananciasDTO {
    private BigDecimal hoy;
    private BigDecimal semana;
    private BigDecimal mes;
}
