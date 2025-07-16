package com.mx.mitienda.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.model.dto.LoginDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginEndpointShouldReturnToken() {
        LoginDTO login = new LoginDTO();
        login.setEmail("admin@example.com");
        login.setPassword("1234");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginDTO> request = new HttpEntity<>(login, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/auth/login", request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("token"));
    }
}