package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductoDTO {

    private String productName;
    private BigDecimal totalQuantity;
    private LocalDateTime ultimaVenta;
    private String categoria;
    private String tipoNegocio;
    private LocalDateTime saleDate;
    private String username;
    private String branchName;
    public TopProductoDTO(String productName, BigDecimal totalQuantity) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
    }
    public TopProductoDTO(
            String productName,
            BigDecimal totalQuantity,
            LocalDateTime ultimaVenta,
            String categoria,
            String tipoNegocio,
            String username,
            String branchName
    ) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.ultimaVenta = ultimaVenta;
        this.categoria = categoria;
        this.tipoNegocio = tipoNegocio;
        this.username = username;
        this.branchName = branchName;
    }

}
