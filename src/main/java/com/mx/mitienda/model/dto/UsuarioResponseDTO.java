package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.Rol;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private Rol role;
    private String email;
    private boolean active;
    private Long branchId;
    private String branchName;
    private String businessType;
}
