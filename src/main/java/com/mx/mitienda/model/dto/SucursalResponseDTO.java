package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class SucursalResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Boolean active;
}
