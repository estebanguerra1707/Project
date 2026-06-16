package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {
    private Long id;
    private String clientName;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    List<DetalleVentaResponseDTO> details;
    private Long paymentMethodId;
    private String paymentName;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private String amountInWords;
    private String userName;
    private Boolean active;
    private String rowId;
    private String rowType;
    private String folioDisplay;
    private Boolean consolidated;
    private Long weeklyTicketId;
    private LocalDateTime consolidatedAt;
    private LocalDateTime periodStartDate;
    private LocalDateTime periodEndDate;
    private String periodDisplay;

    private List<Long> ventaIdsConsolidadas;
    private Integer totalVentasConsolidadas;
    private BigDecimal totalPaid;
    private BigDecimal pendingBalance;
    private String paymentStatus;

}
