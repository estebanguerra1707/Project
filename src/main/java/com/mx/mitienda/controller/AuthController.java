package com.mx.mitienda.controller;

import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.model.dto.UsuarioDTO;
import com.mx.mitienda.model.dto.UsuarioResponseDTO;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AUTH", description = "Operaciones de autenticación y registro de usuario")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Tag(name = "LOGIN", description = "Operaciones relacionadas con login a la app")

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UsuarioResponseDTO usuarioResponse = usuarioService.findByEmailUser(userDetails.getUsername());
        return buildAuthResponse(usuarioResponse, userDetails);
    }

    @Tag(name = "REGISTER", description = "Operaciones relacionadas con registrar usuarioa en la app")
    @PostMapping("/register")
    public Map<String, Object> save(@RequestBody @Valid RegisterRequestDTO request){
        Usuario newUser = Usuario.builder()
                .username(request.getUserName())
                .email(request.getEmail())
                .password(request.getPassword()) // Password se codifica dentro del servicio
                .role(request.getRole())
                .active(true)
                .build();

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setUsername(request.getUserName());
        usuarioDTO.setEmail(request.getEmail());
        usuarioDTO.setPassword(request.getPassword());

        UsuarioResponseDTO registrado = usuarioService.registerUser(usuarioDTO);
        UserDetails userDetails = new User(
                registrado.getEmail(),
                newUser.getPassword(), // Usas la original NO codificada porque ya se codificó en el service
                new ArrayList<>() // o authorities según tu lógica de roles
        );
        return buildAuthResponse(registrado, userDetails);

    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Nada que invalidar en servidor para JWT stateless.
        return ResponseEntity.ok("Token invalidado (cliente debe borrarlo).");
    }

    @Tag(name = "TEST", description = "Operaciones relacionadas con test a la app")
    @GetMapping("/test")
    public String test(org.springframework.security.core.Authentication authentication) {
        return "Autenticado como: " + authentication.getName();
    }

    private Map<String, Object> buildAuthResponse(UsuarioResponseDTO usuario, UserDetails userDetails) {
        System.out.println("Rol: " + usuario.getRole());
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Id: " + usuario.getId());

        Map<String, Object> claims = Map.of(
                "rol", usuario.getRole(),
                "email", usuario.getEmail(),
                "id", usuario.getId()
        );

        String token = jwtService.generateToken(claims, userDetails);

        return Map.of(
                "token", token,
                "username", usuario.getUsername(),
                "email", usuario.getEmail(),
                "rol", usuario.getRole(),
                "id", usuario.getId()
        );
    }
}
