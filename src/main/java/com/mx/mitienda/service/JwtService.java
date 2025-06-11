package com.mx.mitienda.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String genToken(UserDetails userDetails){
        return generateToken(new HashMap<>(),userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())//fecha actual
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) //1 dia
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }
    
    public String getEmailUser(String token){
        return getClaim(token, Claims::getSubject);
    }
    //claim es informacion
    //Function para extraer lo que quieras: subject, expiration, roles, etc.
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims getAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public boolean isAValidToken(String token, String email){
        final String user = getEmailUser(token);
        return (user.equals(email) && !isExpired(token));
    }

    private boolean isExpired(String token){
        return getClaim(token, Claims::getExpiration).before(new Date());
    }
}
