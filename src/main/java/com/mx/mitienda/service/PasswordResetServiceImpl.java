package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.PasswordResetAudit;
import com.mx.mitienda.model.PasswordResetToken;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.repository.PasswordResetAuditRepository;
import com.mx.mitienda.repository.PasswordResetTokenRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements IPasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final MailService mailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetAuditRepository auditRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${valid.token.hour}")
    private String validTokenHour;

    @Value("${url.login}")
    private String login;

    @Override
    @Transactional
    public void createToken(String email, String ip) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return;

        Usuario usuario = usuarioOpt.get();

        // BORRAR TOKEN ANTERIOR CORRECTAMENTE
        passwordResetTokenRepository.deleteByUsuario(usuario);

        // ANTI BRUTE FORCE
        long attempts = auditRepo.countRecentByIp(ip, LocalDateTime.now().minusHours(1));
        if (attempts >= 3) {
            throw new IllegalArgumentException("TOO_MANY_REQUESTS");
        }

        auditRepo.save(new PasswordResetAudit(null, email, ip, LocalDateTime.now()));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                null,
                token,
                LocalDateTime.now().plusHours(Long.parseLong(validTokenHour)),
                false,
                usuario
        );

        passwordResetTokenRepository.save(resetToken);

        String url = login + "/reset-password?token=" + token;
        String html = loadTemplate(url);

        mailService.sendEmail(usuario.getEmail(), "Recuperación de contraseña", html);
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token inválido o expirado"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("TOKEN_EXPIRED");
        }

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("TOKEN_ALREADY_USED");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        // Marcar como usado
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Enviar correo de confirmación
        String html = """
            <p>Tu contraseña fue restablecida exitosamente.</p>
            <p><a href="%s">Ingresa al sistema</a></p>
        """.formatted(login);

        mailService.sendEmail(usuario.getEmail(), "Contraseña reestablecida", html);
    }

    private String loadTemplate(String url) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/reset-password.html");
            String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return html.replace("{{url}}", url);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la plantilla HTML", e);
        }
    }
}
