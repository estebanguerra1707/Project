package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class GanaciaDiaDTO {
    private LocalDate fecha;
    private BigDecimal ganancia;
}
