package com.mx.mitienda.service;


import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.UserMapper;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.UpdateUserDTO;
import com.mx.mitienda.model.dto.UsuarioDTO;
import com.mx.mitienda.model.dto.UsuarioResponseDTO;
import com.mx.mitienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UserMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getAll() {
        return usuarioRepository.findByActiveTrue()
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO getById(Long id) {
        Usuario usuario = usuarioRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        return usuarioMapper.toResponse(usuario);
    }
    @Transactional
    public UsuarioResponseDTO registerUser(UsuarioDTO dto) {
        usuarioMapper.validateCreateDTO(dto);
        Usuario usuario = usuarioMapper.toEntity(dto);
        Usuario saved = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(saved);

    }
    @Transactional
    public UsuarioResponseDTO updateUser(Long id, UpdateUserDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));

        usuarioMapper.updateEntity(usuario, usuarioDTO);

        if (usuarioDTO.getNewPassword() != null && !usuarioDTO.getNewPassword().isBlank()) {

            if (usuarioDTO.getCurrentPassword() == null || usuarioDTO.getCurrentPassword().isBlank()) {
                throw new RuntimeException("Debes ingresar la contraseña actual");
            }

            if (!passwordEncoder.matches(usuarioDTO.getCurrentPassword(), usuario.getPassword())) {
                throw new RuntimeException("La contraseña actual es incorrecta");
            }

            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getNewPassword()));
        }
        usuarioRepository.save(usuario);
        Usuario fresh = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado después de actualizar"));

        return usuarioMapper.toResponse(fresh);
    }

    public void logicUserErase(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActive(false);
        usuarioRepository.save(usuario);
    }
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listActive() {
        return usuarioRepository.findByActiveTrue()
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listInactive() {
        return usuarioRepository.findByActiveFalse()
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public UsuarioResponseDTO findByEmailUser(String email) {
        Usuario usuario = usuarioRepository.findByEmailAndActiveTrue(email).orElseThrow(() -> new NotFoundException(
                "No se ha encontrado el uusario, favor de validar"));
        return usuarioMapper.toResponse(usuario);
    }
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmailAndActiveTrue(email);
    }
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getByUsernameActive(String username) {
        Usuario usuario = usuarioRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return usuarioMapper.toResponse(usuario);
    }
    @Transactional(readOnly = true)
    public Optional<Usuario> getByUsername(String username) {
        return usuarioRepository.findByEmailAndActiveTrue(username);
    }
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getUsuariosBySucursal(Long branchId) {
        return usuarioRepository.findByBranchId(branchId)
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

}
