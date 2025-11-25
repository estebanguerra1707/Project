package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.UpdateUserDTO;
import com.mx.mitienda.model.dto.UsuarioDTO;
import com.mx.mitienda.model.dto.UsuarioResponseDTO;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMapper {
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;

    public Usuario toEntity(UsuarioDTO usuarioDTO) {
        Sucursal sucursal = new Sucursal();
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ya registrado");
        }

        if (usuarioRepository.findByUsername(usuarioDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username ya registrado");
        }
        Rol rol = Rol.valueOf(String.valueOf(usuarioDTO.getRole()));
        if (usuarioDTO.getBranchId() != null) {
             sucursal = sucursalRepository.findByIdAndActiveTrue(usuarioDTO.getBranchId()).orElseThrow(()-> new NotFoundException("No se ha encontrado la sucursal, intenta con otra"));
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setRole(rol);
        usuario.setActive(true);
        if (rol.equals(Rol.SUPER_ADMIN)) {
            usuario.setBranch(null);
        }else{
            Sucursal branch = sucursalRepository.findById(sucursal.getId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
            usuario.setBranch(branch);
        }

        return usuario;
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setRole(usuario.getRole());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActive(Boolean.TRUE.equals(usuario.getActive()));

        log.info("dto.email={}", dto.getEmail());
        var branch = usuario.getBranch();
        if (branch != null) {
            dto.setBranchId(branch.getId());
            dto.setBranchName(branch.getName());
        } else {
            // SUPER_ADMIN normalmente no tiene sucursal
            dto.setBranchId(null);
            dto.setBranchName(null); // o "ALL", según lo que prefiera el frontend
        }
        return  dto;
    }

    public void updateEntity(Usuario current, UpdateUserDTO updated) {

        if (updated.getUsername() != null) {
            current.setUsername(updated.getUsername());
        }

        if (updated.getEmail() != null) {
            current.setEmail(updated.getEmail());
        }

        if (updated.getRole() != null) {
            current.setRole(updated.getRole());
        }

        if (updated.getBranchId() != null) {
            Sucursal branch = sucursalRepository.findById(updated.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + updated.getBranchId()));
            current.setBranch(branch);
        }
    }

    public void validateCreateDTO(UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getUsername() == null || usuarioDTO.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio.");
        }
        if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        // Validación de formato de correo (por si no confías solo en la anotación @Email)
        if (!usuarioDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }
        if (usuarioDTO.getRole() == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
        if (usuarioDTO.getPassword() == null || usuarioDTO.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (usuarioDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        // Si el rol requiere una sucursal (ADMIN o VENDOR)
        if ((usuarioDTO.getRole() == Rol.ADMIN || usuarioDTO.getRole() == Rol.VENDOR)
                && usuarioDTO.getBranchId() == null) {
            throw new IllegalArgumentException("La sucursal es obligatoria para este tipo de usuario.");
        }
    }
}
