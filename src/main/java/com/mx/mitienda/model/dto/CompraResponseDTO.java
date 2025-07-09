package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Long id;
    private String providerName;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private List<DetalleCompraResponseDTO> details;
    private Long paymentMethodId;
    private String paymentName;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private String amountInWords;
    private Long userId;
    private String userName;

}
