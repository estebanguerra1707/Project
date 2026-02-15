package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventarioAlertasDTO {
    private Long id;
    private String productName;
    private BigDecimal stock;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private Boolean stockCritico;
    private String branchName;
}
