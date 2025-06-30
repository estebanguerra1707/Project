package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private String providerName;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private List<DetalleCompraResponseDTO> details;
}
