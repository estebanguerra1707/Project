package com.mx.mitienda;

import com.mx.mitienda.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PasswordRecoveryIntegrationTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Test
    void shouldSendResetTokenEmail() {
        assertDoesNotThrow(() -> passwordResetService.initiatePasswordReset("admin@example.com"));
    }
}