package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class InventarioAlertasDTO {
    private Long id;
    private String productName;
    private Integer stock;
    private Integer minStock;
    private Integer maxStock;
    private Boolean stockCritico;
    private String branchName;
}
