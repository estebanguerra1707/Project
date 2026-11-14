package com.mx.mitienda.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WhatsAppApiClient {

    // Extraemos las credenciales que sacamos de la consola de Meta
    @Value("${meta.whatsapp.access-token}")
    private String apiToken;

    @Value("${meta.whatsapp.phone-number-id}")
    private String phoneNumberId;
    private final RestTemplate restTemplate;

    public WhatsAppApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Envía un mensaje de texto normal
     */
    public void sendTextMessage(String to, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", to);
        body.put("type", "text");
        body.put("text", Map.of("body", message));

        executePostRequest(body);
    }

    /**
     * Envía una imagen (Ej. el menú de Los Rellenitos)
     */
    public void sendImageMessage(String to, String imageUrl, String caption) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", to);
        body.put("type", "image");
        body.put("image", Map.of("link", imageUrl, "caption", caption));

        executePostRequest(body);
    }

    /**
     * Envía un sticker (Ej. "Su orden está en preparación")
     */
    public void sendStickerMessage(String to, String stickerId) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", to);
        body.put("type", "sticker");
        body.put("sticker", Map.of("id", stickerId));

        executePostRequest(body);
    }

    /**
     * Método central que dispara la petición HTTP a los servidores de Meta
     */
    private void executePostRequest(Map<String, Object> body) {
        String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        // Aquí también usamos los corchetes angulares correctos para evitar el error de tipado
        var request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Mensaje enviado con éxito: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Error al enviar mensaje de WhatsApp: " + e.getMessage());
        }
    }
}