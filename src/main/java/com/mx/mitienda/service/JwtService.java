package com.mx.mitienda.service;

import com.mx.mitienda.security.JwtSecretProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${JWT_EXPIRATION_MS:3600000}")
    private long expirationMs;

    @Value("${JWT_REFRESH_EXPIRATION_MS:604800000}")
    private long refreshExpirationMs;

    private Key signingKey;
    private final JwtSecretProvider secretProvider;

    // Si guardaste el secreto en Base64 en AWS, pon true en application.properties:
    @Value("${mi-tienda.jwt.secret-base64:false}")
    private boolean secretIsBase64;

    @PostConstruct
    public void init() {
        // 1) Lée el secreto DESDE Secrets Manager
        String rawSecret = secretProvider.get();
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalStateException("JWT secret vacío (Secrets Manager).");
        }

        // 2) Si lo guardaste en Base64, decodifica; si no, usa bytes UTF-8
        byte[] keyBytes = secretIsBase64
                ? Base64.getDecoder().decode(rawSecret)
                : rawSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) { // 256 bits mínimo para HS256
            throw new IllegalArgumentException("JWT secret debe ser ≥ 32 bytes (256 bits). Actual: " + keyBytes.length);
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("🔐 Llave JWT inicializada ({} bytes). Exp: {} ms", keyBytes.length, expirationMs);
    }

    public String genToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // email/username
                .setIssuedAt(now)
                .setExpiration(exp)
                // Para JJWT 0.11.x:
                .signWith(signingKey, SignatureAlgorithm.HS256)
                // Para JJWT 0.12.x alternativa:
                // .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        log.debug("JWT emitido para {}", userDetails.getUsername());
        return token;
    }


    public String getEmailUser(String token) {
        try {
            return getClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.out.println("Error extrayendo email del token: " + e.getMessage());
            return null;
        }
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public boolean isAValidToken(String token, String email) {
        final String user = getEmailUser(token);
        return (user.equals(email) && !isExpired(token));
    }

    private boolean isExpired(String token) {
        Date expiration = getClaim(token, Claims::getExpiration);
        boolean expired = expiration.before(new Date());
        if (expired) {
            System.out.println(" El token expiró en: " + expiration);
        }
        return expired;
    }

    public String generateAccessToken(Map<String, Object> claims, UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public boolean isValidRefreshToken(String token) {
        try {
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

}
