package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class SucursalResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Boolean active;
    private Long businessTypeId;
    private String businessTypeName;
    private Boolean isAlertaStockCritico;
    private Boolean usaInventarioPorDuenio;
}
