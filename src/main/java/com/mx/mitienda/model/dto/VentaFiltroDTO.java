package com.mx.mitienda.model.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VentaFiltroDTO {
    private Long id;
    private Boolean active;
    private Long clienteId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal totalAmount;
    private Integer month;
    private Integer year;
    private Integer day;
    private Long paymentMethodId;
}
