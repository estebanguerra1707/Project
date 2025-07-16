package com.mx.mitienda;

import com.mx.mitienda.model.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private AuthenticationManager authService;

    @Test
    void shouldAuthenticateSuccessfully() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("123456"); // Asume que está hasheado o se mockea

        assertDoesNotThrow(() -> authService.authenticate((Authentication) loginRequest));
    }
}