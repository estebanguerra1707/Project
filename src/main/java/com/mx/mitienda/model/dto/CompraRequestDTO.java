package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraRequestDTO {
    private Long providerId;
    private Long branchId;
    private LocalDateTime purchaseDate;
    private Long paymentMethodId;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private String amountInWords;
    private List<String> emailList;
    private boolean isPrinted;
    private List<DetalleCompraRequestDTO> details;
}
