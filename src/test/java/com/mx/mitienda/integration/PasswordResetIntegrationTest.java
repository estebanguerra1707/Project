package com.mx.mitienda.integration;

import com.mx.mitienda.model.dto.EmailDTO;
import com.mx.mitienda.model.dto.ResetPasswordDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PasswordResetIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldInitiatePasswordReset() {
        EmailDTO dto = new EmailDTO();
        dto.setEmail("admin@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmailDTO> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/auth/reset-password", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}