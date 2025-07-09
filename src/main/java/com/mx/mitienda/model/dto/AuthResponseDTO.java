package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String username;
    private String email;
    private Rol rol;
    private Long id;
}