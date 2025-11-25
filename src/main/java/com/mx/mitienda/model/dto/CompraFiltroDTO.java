package com.mx.mitienda.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompraFiltroDTO {
    private Long purchaseId;
    private Boolean active;
    private Long supplierId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]")
    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]")
    private LocalDateTime end;
    private BigDecimal min;
    private BigDecimal max;
    private Integer day;
    private Integer month;
    private Integer year;
}
