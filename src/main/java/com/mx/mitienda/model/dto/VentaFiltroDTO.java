package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VentaFiltroDTO {
    private Boolean active;
    private String clientName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal totalAmount;
    private Integer month;
    private Integer year;
    private Integer day;
    private Long id;
    private Long paymentMethodId;
}
