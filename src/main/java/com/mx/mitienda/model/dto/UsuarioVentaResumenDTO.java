package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class UsuarioVentaResumenDTO {
    private Long userId;
    private String username;

    // Piezas vendidas
    private BigDecimal totalQuantity;

    // Bruto vendido
    private BigDecimal totalIncome;

    // Ganancia real
    private BigDecimal netProfit;

    private Long salesCount;

    // Constructor para dashboard mensual / diario por usuario
    public UsuarioVentaResumenDTO(
            Long userId,
            String username,
            BigDecimal totalIncome,
            BigDecimal netProfit,
            Long salesCount
    ) {
        this.userId = userId;
        this.username = username;
        this.totalQuantity = BigDecimal.ZERO;
        this.totalIncome = totalIncome;
        this.netProfit = netProfit;
        this.salesCount = salesCount;
    }

    // Constructor para top productos vendido por usuario
    public UsuarioVentaResumenDTO(
            Long userId,
            String username,
            BigDecimal totalQuantity,
            BigDecimal totalIncome,
            BigDecimal netProfit,
            Long salesCount
    ) {
        this.userId = userId;
        this.username = username;
        this.totalQuantity = totalQuantity;
        this.totalIncome = totalIncome;
        this.netProfit = netProfit;
        this.salesCount = salesCount;
    }
}