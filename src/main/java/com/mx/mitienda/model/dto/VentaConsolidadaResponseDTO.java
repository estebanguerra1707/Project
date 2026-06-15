package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaConsolidadaResponseDTO {

    private Long clienteId;

    private String clientName;

    private Long userId;

    private String userName;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime generatedAt;

    private List<Long> ventaIds;

    private Integer totalVentas;

    private List<VentaConsolidadaProductoDTO> productos;

    private BigDecimal totalAmount;

    private String amountInWords;

    private Long weeklyTicketId;
    private LocalDateTime consolidatedAt;
}