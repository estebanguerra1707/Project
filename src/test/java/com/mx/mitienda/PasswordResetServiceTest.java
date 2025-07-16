package com.mx.mitienda.service;

import com.mx.mitienda.model.PasswordResetToken;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.repository.PasswordResetTokenRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.service.PasswordResetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Test
    void shouldReturnValidToken() {
        Usuario user = new Usuario();
        user.setId(1L);
        user.setEmail("test@mail.com");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("abc123");
        token.setUsuario(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(passwordResetTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        passwordResetService.resetPassword("abc123", "123465");
      
    }
}