package com.mx.mitienda.controller;

import com.mx.mitienda.model.Rol;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.JwtResponse;
import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import jakarta.validation.Valid;
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
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/login")
    public  Map<String, Object> login(@RequestBody @Valid LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        //Obtener detalles del usuario
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.findByEmailUser(userDetails.getUsername());

        // Agregar claims personalizados
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("email", usuario.getEmail());
        claims.put("id", usuario.getId());

        //  Generar token JWT
        String token = jwtService.generateToken(claims, userDetails);

        // Armar respuesta
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
        // 1. Verificar si el usuario ya existe
        Usuario oldUser = usuarioService.findByEmailUser(request.getEmail());

           if(oldUser!= null && oldUser.getActive()){
               throw new IllegalArgumentException("El correo ya esta en uso, intenta con otro");
           }
            Usuario newUser = new Usuario();

            newUser.setUsername(request.getEmail());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
            newUser.setActive(true);
            newUser.setRol(request.getRol());//cambiar

        Usuario register = usuarioService.save(newUser);
        //  Crear claims personalizados
        Map<String, Object> claims = new HashMap<>();

        claims.put("rol", register.getRol().name());
        claims.put("email", register.getEmail());
        claims.put("id", register.getId());
        //Generar toker JWT
        UserDetails userDetails = new User(register.getEmail(), register.getPassword(), List.of());

        String token = jwtService.generateToken(claims, userDetails);

        Map<String, Object> response = new HashMap<>();

        response.put("token", token);
        response.put("username", register.getUsername());
        response.put("rol", register.getRol());
        response.put("id", register.getId());

        return  response;
        }

    @GetMapping("/auth/test")
    public String test(org.springframework.security.core.Authentication authentication) {
        return "Autenticado como: " + authentication.getName();
    }
}
