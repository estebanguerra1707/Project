package com.mx.mitienda.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void dashboardShouldBeProtected() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer fake-token");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/dashboard/stock-critico", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}