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
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.updateUser(id, usuarioDTO));
    }
    @GetMapping("/sucursal/{branchId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> getBySucursal(@PathVariable Long branchId) {
        return ResponseEntity.ok(usuarioService.getUsuariosBySucursal(branchId));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Si usaras sesión tradicional:
        request.getSession().invalidate();
        // Con JWT, no hay nada que invalidar en servidor (a menos que guardes blacklist).
        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Enviar enlace de recuperación")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody EmailDTO emailDTO) {
        passwordResetService.createToken(emailDTO.getEmail());
        return ResponseEntity.ok("Se ha enviado el enlace para restablecer la contraseña");
    }

    @Operation(summary = "Restablecer contraseña con token")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
        passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

}
