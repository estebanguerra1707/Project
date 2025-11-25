package com.mx.mitienda.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SucursalDTO {
    @NotBlank private String name;
    @NotBlank private String address;
    @NotBlank
    private String phone;
    private Boolean isAlertaStockCritico = Boolean.FALSE;
    @NotNull
    @Min(1)
    private Long businessTypeId;
}
