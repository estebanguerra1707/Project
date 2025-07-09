package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class InventarioSucursalResponseDTO {
    private Long id;
    private String productName;
    private String branchName;
    private Integer quantity;
}