package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VentaFiltroDTO {
    private Boolean active;
    private String client;
    private LocalDateTime start;
    private LocalDateTime end;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal total;
    private Integer month;
    private Integer year;
    private Integer day;
    private Long id;
}
