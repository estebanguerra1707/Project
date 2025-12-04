package com.mx.mitienda.controller;

import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.model.dto.UsuarioDTO;
import com.mx.mitienda.model.dto.UsuarioResponseDTO;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import com.mx.mitienda.util.enums.Rol;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),        // o getUsername() si tu servicio usa username
                            loginRequest.getPassword()
                    )
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UsuarioResponseDTO usuarioResponse =
                    usuarioService.findByEmailUser(userDetails.getUsername());
            if (usuarioResponse == null) {
                log.warn("Usuario no encontrado tras autenticar: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "bad_credentials"));
            }
            // En el servicio o en el AuthController, justo después de cargar el usuario:
            if (usuarioResponse.getRole() != Rol.SUPER_ADMIN && usuarioResponse.getBranchName() == null) {
                throw new IllegalArgumentException("El usuario debe tener una sucursal asignada");
            }

            Map<String, Object> body = buildAuthResponse(usuarioResponse, userDetails);
            return ResponseEntity.ok(body);

        } catch (BadCredentialsException |
                 UsernameNotFoundException ex) {
            log.warn("Credenciales inválidas para {}: {}", loginRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "bad_credentials"));
        } catch (DisabledException ex) {
            log.warn("Usuario deshabilitado {}: {}", loginRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "user_disabled"));
        } catch (Exception ex) {
            log.error("Fallo inesperado en /auth/login", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "server_error"));
        }
    }


    @Tag(name = "REGISTER", description = "Operaciones relacionadas con registrar usuario en la app")
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
        usuarioDTO.setBranchId(request.getBranchId());
        usuarioDTO.setRole(request.getRole());

        UsuarioResponseDTO registrado = usuarioService.registerUser(usuarioDTO);
        UserDetails userDetails = new User(
                registrado.getEmail(),
                newUser.getPassword(), // Usas la original NO codificada porque ya se codificó en el service
                new ArrayList<>() // o authorities según tu lógica de roles
        );
        return buildAuthResponse(registrado, userDetails);

    }
    @PostMapping("/register-sa")
    public ResponseEntity<?> registerSuperAdmin(@RequestBody RegisterRequestDTO request) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setUsername(request.getUserName());
        usuarioDTO.setEmail(request.getEmail());
        usuarioDTO.setPassword(request.getPassword());
        usuarioDTO.setBranchId(request.getBranchId());
        usuarioDTO.setRole(Rol.SUPER_ADMIN);
        request.setBranchId(null);
        usuarioService.registerUser(usuarioDTO);
        return ResponseEntity.ok("Super Admin creado exitosamente");
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "missing_refresh_token"));
        }

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_refresh_token"));
        }

        String email = jwtService.getEmailUser(refreshToken);
        UserDetails user = usuarioService.loadUserByUsername(email);

        String newAccess = jwtService.generateAccessToken(new HashMap<>(), user);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess
        ));
    }

    private Map<String, Object> buildAuthResponse(UsuarioResponseDTO usuario, UserDetails userDetails) {

        Map<String, Object> claims = Map.of(
                "rol", usuario.getRole(),
                "email", usuario.getEmail(),
                "id", usuario.getId()
        );

        String accessToken = jwtService.generateAccessToken(claims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", accessToken);
        body.put("refreshToken", refreshToken);
        body.put("username", usuario.getUsername());
        body.put("email", usuario.getEmail());
        body.put("rol", usuario.getRole());
        body.put("id", usuario.getId());
        body.put("branchId", usuario.getBranchId());        // <- ajusta al nombre real del getter
        body.put("businessType", usuario.getBusinessType()); // <- idem
        body.put("branchName", usuario.getBranchName());    // si lo tienes

        return body;
    }
}
