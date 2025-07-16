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
    private List<String> emailList;
    private Boolean isPrinted;
    List<DetalleVentaRequestDTO> details;
    private Long paymentMethodId;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
}
