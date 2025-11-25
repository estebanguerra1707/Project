package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @NotBlank(message = "El nombre del usuario es obligatorio")
    private String username;

    @Email(message = "El correo no es válido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private Rol role;

    private Long branchId;

    // Cambiar contraseña (opcional)
    private String currentPassword;
    private String newPassword;
}
