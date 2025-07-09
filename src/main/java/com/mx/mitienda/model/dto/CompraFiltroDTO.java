package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompraFiltroDTO {
    private Boolean active;
    private String supplier;
    private LocalDateTime start;
    private LocalDateTime end;
    private BigDecimal min;
    private BigDecimal max;
    private Integer day;
    private Integer month;
    private Integer year;
}
