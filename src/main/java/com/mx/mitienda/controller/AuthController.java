package com.mx.mitienda.controller;

import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Tag(name = "LOGIN", description = "Operaciones relacionadas con login a la app")

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.findByEmailUser(userDetails.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRole().name());
        claims.put("email", usuario.getEmail());
        claims.put("id", usuario.getId());

        String token = jwtService.generateToken(claims, userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", usuario.getUsername());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRole());
        response.put("id", usuario.getId());
        return response;
    }

    @Tag(name = "REGISTER", description = "Operaciones relacionadas con registrar usuarioa en la app")
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody @Valid RegisterRequestDTO request){
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // NO encodear aquí
        newUser.setActive(true);
        newUser.setRole(request.getRole());

        Usuario register = usuarioService.registerUser(newUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", register.getRole().name());
        claims.put("email", register.getEmail());
        claims.put("id", register.getId());

        UserDetails userDetails = new User(register.getEmail(), register.getPassword(), register.getAuthorities());
        String token = jwtService.generateToken(claims, userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", register.getUsername());
        response.put("rol", register.getRole());
        response.put("id", register.getId());

        return response;
    }

    @Tag(name = "TEST", description = "Operaciones relacionadas con test a la app")
    @GetMapping("/test")
    public String test(org.springframework.security.core.Authentication authentication) {
        return "Autenticado como: " + authentication.getName();
    }
}
