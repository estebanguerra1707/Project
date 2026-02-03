package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProveedorResponseDTO {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private Long branchId;
    private String branchName;
    private List<SucursalAsignadaDTO> sucursales;
}
