package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioVentaResumenDTO {

    private Long userId;
    private String username;
    private BigDecimal totalQuantity;
    private BigDecimal totalIncome;
    private Long salesCount;

    public UsuarioVentaResumenDTO(
            Long userId,
            String username,
            BigDecimal totalIncome,
            Long salesCount
    ) {
        this.userId = userId;
        this.username = username;
        this.totalQuantity = BigDecimal.ZERO;
        this.totalIncome = totalIncome;
        this.salesCount = salesCount;
    }
}