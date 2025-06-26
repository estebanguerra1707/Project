package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import lombok.Data;

@Data
public class UsuarioDTO {
    private String username;
    private String email;
    private String password; // opcional, cambiar solo si se manda
}
