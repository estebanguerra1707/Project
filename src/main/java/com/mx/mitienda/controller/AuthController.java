package com.mx.mitienda.controller;

import com.mx.mitienda.model.Rol;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.JwtResponse;
import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.findByEmailUser(userDetails.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("email", usuario.getEmail());
        claims.put("id", usuario.getId());

        String token = jwtService.generateToken(claims, userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", usuario.getUsername());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());
        response.put("id", usuario.getId());
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody @Valid RegisterRequestDTO request){
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // NO encodear aquí
        newUser.setActive(true);
        newUser.setRol(request.getRol());

        Usuario register = usuarioService.registerUser(newUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", register.getRol().name());
        claims.put("email", register.getEmail());
        claims.put("id", register.getId());

        UserDetails userDetails = new User(register.getEmail(), register.getPassword(), register.getAuthorities());
        String token = jwtService.generateToken(claims, userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", register.getUsername());
        response.put("rol", register.getRol());
        response.put("id", register.getId());

        return response;
    }

    @GetMapping("/test")
    public String test(org.springframework.security.core.Authentication authentication) {
        return "Autenticado como: " + authentication.getName();
    }
}
