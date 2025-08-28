package com.mx.mitienda.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ActuatorApiKeyFilter extends OncePerRequestFilter {

    @Value("${health.api.token:}")
    private String expectedToken;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        if (!path.startsWith("/actuator")) return true; // Solo /actuator/**
        // Health y subrutas de health quedan públicas
        if (path.equals("/actuator/health") || path.startsWith("/actuator/health/")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Solo GET en actuator para reducir superficie (ajusta si necesitas más)
        if (!HttpMethod.GET.matches(req.getMethod())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Method not allowed");
            return;
        }

        if (expectedToken == null || expectedToken.isBlank()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Actuator token not configured");
            return;
        }

        String token = req.getHeader("X-Health-Token");
        if (token == null || !token.equals(expectedToken)) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid actuator token");
            return;
        }

        // Autenticar con ROLE_ACTUATOR
        var auth = new UsernamePasswordAuthenticationToken(
                "actuator", null, List.of(new SimpleGrantedAuthority("ROLE_ACTUATOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}
