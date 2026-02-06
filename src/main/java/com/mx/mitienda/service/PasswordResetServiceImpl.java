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

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusMinutes(15);

        long attempts = auditRepo.countRecentByIp(ip, since);
        if (attempts >= 3) {
            LocalDateTime oldest = auditRepo.findOldestAttemptInWindowByIp(ip, since);
            LocalDateTime retryAt = oldest != null ? oldest.plusMinutes(15) : now.plusMinutes(15);
            throw new IllegalArgumentException("TOO_MANY_REQUESTS");
        }

        // 3) Auditar SIEMPRE
        auditRepo.save(new PasswordResetAudit(null, normalizedEmail, ip, now));

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(normalizedEmail);
        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // 5) Borrar token anterior
        passwordResetTokenRepository.deleteByUsuario(usuario);

        // 6) Crear token nuevo
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                null,
                token,
                now.plusHours(Long.parseLong(validTokenHour)),
                false,
                usuario
        );

        passwordResetTokenRepository.save(resetToken);

        // 7) Enviar correo
        String url = login + "/reset-password?token=" + token;
        String html = loadTemplate(url);
        mailService.sendEmail(usuario.getEmail(), "Recuperación de contraseña", html);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("PASSWORD INVALIDA");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("PASSWORD DEMASIADO PEQUEÑA");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_TOKEN"));

        LocalDateTime now = LocalDateTime.now();

        if (resetToken.getExpiryDate().isBefore(now) || resetToken.isUsed()) {
            // mismo error para no dar pistas
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Idealmente after-commit, pero mínimo así:
        String html = """
        <p>Tu contraseña fue restablecida exitosamente.</p>
        <p><a href="%s">Ingresa al sistema</a></p>
    """.formatted(login);

        mailService.sendEmail(usuario.getEmail(), "Contraseña restablecida", html);
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
