package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CompraFiltroDTO {
    private Boolean active;
    private String supplier;
    private LocalDate start;
    private LocalDate end;
    private BigDecimal min;
    private BigDecimal max;
}
