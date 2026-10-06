package com.mx.mitienda.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtSecretProvider.class);

    // Inyectamos directamente el secreto desde las propiedades
    @Value("${mi-tienda.jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("El secreto JWT no está configurado (mi-tienda.jwt.secret).");
        }
        // Validación de seguridad para HS256
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("El secreto JWT es demasiado débil. Debe tener al menos 32 caracteres.");
        }
        log.info("JwtSecretProvider inicializado correctamente usando configuración local.");
    }

    // Mantenemos el nombre del método get() para no romper JwtService
    public String get() {
        return jwtSecret;
    }
}