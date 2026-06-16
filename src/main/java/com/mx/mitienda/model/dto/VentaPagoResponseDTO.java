package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VentaPagoResponseDTO {
    private Long id;
    private Long ventaId;
    private BigDecimal amount;
    private Long paymentMethodId;
    private String paymentName;
    private String userName;
    private LocalDateTime paymentDate;
    private String note;
    private Boolean active;
}