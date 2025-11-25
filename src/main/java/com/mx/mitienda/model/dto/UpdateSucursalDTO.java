package com.mx.mitienda.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSucursalDTO {
    @NotNull
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Boolean isAlertaStockCritico;
    private Long businessTypeId;
}
