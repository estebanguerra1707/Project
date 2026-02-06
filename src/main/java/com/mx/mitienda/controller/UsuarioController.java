package com.mx.mitienda.controller;

import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.service.IPasswordResetService;
import com.mx.mitienda.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "USUARIOS", description = "Operaciones relacionadas con usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final IPasswordResetService passwordResetService;


    @Operation(summary = "Registrar nuevo usuario")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> register(@RequestBody UsuarioDTO usuarioRegisterDTO) {
        usuarioService.registerUser(usuarioRegisterDTO);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @Tag(name = "USUARIOS GET ALL", description = "Obtener todos los usuarios")
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }

    @Tag(name = "USUARIOS GET BY ID", description = "Obtener un usuario por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }

    @Tag(name = "USUARIOS DELETE LOGICO", description = "Desactivar un usuario")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        usuarioService.logicUserErase(id);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "USUARIOS UPDATE", description = "Actualizar usuario con DTO limpio")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable Long id, @RequestBody UpdateUserDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.updateUser(id, usuarioDTO));
    }
    @GetMapping("/sucursal/{branchId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> getBySucursal(@PathVariable Long branchId) {
        return ResponseEntity.ok(usuarioService.getUsuariosBySucursal(branchId));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Enviar enlace de recuperación")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody EmailDTO emailDTO,
                                                 HttpServletRequest request) {
        try {
            passwordResetService.createToken(emailDTO.getEmail(), getClientIp(request));
            return ResponseEntity.ok("Si el correo existe, se enviará un enlace para restablecer la contraseña");
        } catch (IllegalArgumentException ex) {
            if ("TOO_MANY_REQUESTS".equals(ex.getMessage())) {
                // No revela si existe el correo, solo dice que hay rate limit
                return ResponseEntity.status(429)
                        .body("Demasiadas solicitudes. Intenta nuevamente más tarde.");
            }
            throw ex;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        // Si estás detrás de proxy / load balancer, esto es importante
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "Restablecer contraseña con token")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestBody ResetPasswordDTO dto
    ) {
        passwordResetService.resetPassword(token, dto.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

}
