package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VentaFiltroDTO {
    private Boolean active;
    private String client;
    private LocalDate start;
    private LocalDate end;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal total;
    private Integer month;
    private Integer year;
    private Integer day;
    private Long id;
}
