package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioDTO {
    @NotBlank(message = "El nombrte del usaurio es obligatorio")
    private String username;
    @Email(message = "El correo no es válido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;
    @NotBlank(message = "El rol es obligatorio")
    private Rol role;
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    private Long branchId;
}
