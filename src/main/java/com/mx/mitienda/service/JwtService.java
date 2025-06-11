package com.mx.mitienda.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {
    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${JWT_EXPIRATION_MS}")
    private long expirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException(" JWT_SECRET debe tener al menos 32 caracteres.");
        }
        signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        System.out.println("🔐 Llave JWT inicializada correctamente. Longitud: " + secretKey.length());
    }

    public String genToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())  // el email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        System.out.println(" Token generado para: " + userDetails.getUsername());
        return token;
    }


    public String getEmailUser(String token) {
        try {
            return getClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.out.println("⚠ Error extrayendo email del token: " + e.getMessage());
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
}
