package com.mx.mitienda.service;

import com.mx.mitienda.service.IMetaApiClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MetaApiClientServiceImpl implements IMetaApiClientService {

    @Value("${meta.whatsapp.api-url}")
    private String apiUrl;

    @Value("${meta.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${meta.whatsapp.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate;

    public MetaApiClientServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void enviarMensajeTexto(String numeroDestino, String mensaje) {
        String url = apiUrl + "/" + phoneNumberId + "/messages";

        // 1. Construir el JSON exacto que Meta requiere
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", numeroDestino);
        body.put("type", "text");

        Map<String, String> textNode = new HashMap<>();
        textNode.put("body", mensaje);
        body.put("text", textNode);

        // 2. Configurar los Headers (Token de autorización)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // 3. Empaquetar y enviar la petición
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("Mensaje enviado a {}. Respuesta de Meta: {}", numeroDestino, response.getBody());
        } catch (Exception e) {
            log.error("Error al enviar mensaje a {}: {}", numeroDestino, e.getMessage());
        }
    }
}