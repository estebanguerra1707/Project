package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class InventarioAlertaFiltroDTO {
private String productname;
private Long branchId;
private Boolean stocKCritico;
}
