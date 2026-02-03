package com.mx.mitienda.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private String phone;
    private Long sucursalId;
    private Long sucursalesCount;
    private Boolean multiSucursal;

    private Boolean isActive;
    private List<Long> branchIds;
}
