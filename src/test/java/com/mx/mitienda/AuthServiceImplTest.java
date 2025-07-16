package com.mx.mitienda.service;

import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.LoginDTO;
import com.mx.mitienda.model.dto.JwtResponseDTO;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("admin@example.com");
        loginDTO.setPassword("1234");

        Usuario usuario = new Usuario();
        usuario.setEmail("admin@example.com");
        usuario.setPassword("1234");

        when(usuarioRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("fake-jwt");

        JwtResponseDTO response = authService.login(loginDTO);
        assertNotNull(response);
        assertEquals("fake-jwt", response.getToken());
    }

    @Test
    void loginShouldThrowExceptionWhenUserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("admin@example.com");
        loginDTO.setPassword("1234");

        when(usuarioRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginDTO));
    }
}