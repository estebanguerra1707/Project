package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

@Data
public class InventarioAlertaFiltroDTO {
private String productname;
private Long branchId;
private Boolean stocKCritico;
private InventarioOwnerType ownerType;
private Boolean usaInventarioPorDuenio;

}
