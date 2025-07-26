package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @Email(message = "El correo no es válido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol role;

    @Nullable
    private Long branchId;

    @NotBlank(message = "El usuario debe ser obligatorio")
    private String userName;
}
