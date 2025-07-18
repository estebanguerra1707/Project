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

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;

    public Usuario toEntity(UsuarioDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username ya registrado");
        }
        Rol rol = Rol.valueOf(String.valueOf(dto.getRole()));
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(dto.getBranchId()).orElseThrow(()-> new NotFoundException("No se ha encontrado la sucursal, intenta con otra"));
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRole(rol);
        usuario.setActive(true);
        usuario.setBranch(sucursal);
        return usuario;
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setId(usuario.getId());
        usuarioResponseDTO.setRole(usuario.getRole());
        usuarioResponseDTO.setUsername(usuario.getUsername());
        usuarioResponseDTO.setEmail(usuario.getEmail());
        usuarioResponseDTO.setActive(usuario.getActive());
        usuarioResponseDTO.setBranchId(usuario.getBranch().getId());
        usuarioResponseDTO.setBranchName(usuario.getBranch().getName());
        return usuarioResponseDTO;
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
