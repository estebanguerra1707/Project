package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {
    private Long id;
    private String customerName;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    List<DetalleVentaResponseDTO> details;

}
