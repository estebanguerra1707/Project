package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class ClienteFiltroDTO {
    private Long id;
    private Boolean active;
    private String name;
    private String email;
    private String phone;
}
