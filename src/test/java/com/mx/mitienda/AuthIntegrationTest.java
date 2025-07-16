package com.mx.mitienda;

import com.mx.mitienda.model.dto.LoginRequest;
import com.mx.mitienda.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void shouldAuthenticateSuccessfully() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("123456"); // Asume que está hasheado o se mockea

        assertDoesNotThrow(() -> authService.authenticate(loginRequest));
    }
}