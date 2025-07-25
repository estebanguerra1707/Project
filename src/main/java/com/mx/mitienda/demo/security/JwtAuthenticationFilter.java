package com.mx.mitienda.demo.security;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.security.UserContext;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //intercepta cada peticion HTTP
    //verifica si hay un JWT valido en el header
    //como hereda de OncePerRequestFilter, se ejecuta una vez por cada peticion

    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UserContext userContext;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String email;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        email = jwtService.getEmailUser(jwt);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = usuarioService.loadUserByUsername(email);
            // Si hay problemas, se lanza una excepción que captura el GlobalExceptionHandler
            if (jwtService.isAValidToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                Usuario usuario = usuarioService.findByEmail(email).orElseThrow(()->new NotFoundException("No se ha encontrado el usuario por su email..."));
                userContext.setCurrentUser(usuario);
            }
        }

        filterChain.doFilter(request, response);
    }

}
