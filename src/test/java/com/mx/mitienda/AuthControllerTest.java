package com.mx.mitienda;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.controller.AuthController;
import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.model.dto.UsuarioDTO;
import com.mx.mitienda.model.dto.UsuarioResponseDTO;
import com.mx.mitienda.service.JwtService;
import com.mx.mitienda.service.UsuarioService;
import com.mx.mitienda.util.enums.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        UserDetails userDetails = new User("test@example.com", "password", java.util.Collections.emptyList());

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setEmail("test@example.com");
        usuarioResponseDTO.setRole(Rol.valueOf("ADMIN"));
        usuarioResponseDTO.setId(1L);
        usuarioResponseDTO.setUsername("testuser");

        when(usuarioService.findByEmailUser("test@example.com")).thenReturn(usuarioResponseDTO);
        when(jwtService.generateToken(anyMap(), any())).thenReturn("mocked-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegister() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserName("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setRole(Rol.valueOf("ADMIN"));

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setUsername("testuser");
        response.setEmail("test@example.com");
        response.setRole(Rol.valueOf("ADMIN"));
        response.setId(1L);

        when(usuarioService.registerUser(any(UsuarioDTO.class))).thenReturn(response);
        when(jwtService.generateToken(anyMap(), any())).thenReturn("mocked-token");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
