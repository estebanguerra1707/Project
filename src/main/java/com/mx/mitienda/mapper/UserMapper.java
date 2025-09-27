package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.UsuarioDTO;
import com.mx.mitienda.model.dto.UsuarioResponseDTO;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
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
        usuario.setBranch(sucursal);
        return usuario;
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setRole(usuario.getRole());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActive(usuario.getActive());

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

    public void  updateEntity(Usuario current, UsuarioDTO updated) {
        if (updated.getUsername() != null) {
            current.setUsername(updated.getUsername());
        }
        if (updated.getEmail() != null) {
            current.setEmail(updated.getEmail());
        }
        if (updated.getPassword() != null) {
            current.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        if (updated.getRole() != null) {
            current.setRole(updated.getRole());
        }

    }

}
