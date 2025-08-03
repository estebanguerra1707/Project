package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class ProveedorDTO {
    private String name;
    private String contact;
    private String email;
    private Long branchId;
}
