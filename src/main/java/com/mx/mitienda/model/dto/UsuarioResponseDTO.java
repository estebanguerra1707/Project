package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private Rol role;
    private String email;
    private boolean active;
}
