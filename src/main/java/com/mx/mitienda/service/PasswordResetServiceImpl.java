package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.PasswordResetToken;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.repository.PasswordResetTokenRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements IPasswordResetService{

    private final UsuarioRepository usuarioRepository;
    private final MailService mailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${valid.token.hour}")
    private String validTokenHour;

    @Value("${url.login}")
    private String login;

    @Override
    public void createToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(null, token, LocalDateTime.now().plusHours(Long.parseLong(validTokenHour)), usuario);
        passwordResetTokenRepository.save(resetToken);

        String url = login + "/reset-password?token=" + token;
        String html = "<p>Haz clic para restablecer tu contraseña:</p><a href='" + url + "'>Restablecer contraseña</a>";

        mailService.sendEmail(
                usuario.getEmail(),
                "Restablecimiento de contraseña",
                html
        );
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token inválido o expirado"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        passwordResetTokenRepository.delete(resetToken);
        String html = "<p>Tu contrasenia se ha reestablecido correctamente, da click aqui para iniciar sesion:<a href='\" + login + \"'>Ingresa al sistema</a></p>";

        mailService.sendEmail(
                usuario.getEmail(),
                "Contraseña reestablecida",
                html
        );
    }
}
