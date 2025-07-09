package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaRequestDTO {
    Long clientId;
    Long branchId;
    LocalDateTime saleDate;
    List<DetalleVentaRequestDTO> details;
    private Long paymentMethodId;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
}
