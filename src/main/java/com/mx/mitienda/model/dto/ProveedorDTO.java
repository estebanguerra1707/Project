package com.mx.mitienda.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProveedorDTO {
    private String name;
    private String contact;
    private String email;
    private List<Long> branchIds;
}
