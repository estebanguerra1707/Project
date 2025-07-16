package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private String phone;
    private Boolean isActive;
}
