package com.mx.mitienda.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
@Service

public class WhatsAppSenderServiceImpl implements  IWhatsAppSenderService {
    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(WhatsAppSenderServiceImpl.class);


    @Value("${meta.whatsapp.api-url}")
    private String apiUrl;

    @Value("${meta.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${meta.whatsapp.access-token}")
    private String accessToken;

    public WhatsAppSenderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void enviarRespuestaWhatsApp(String numeroDestino, String mensajeTexto) {
        String url = apiUrl + "/" + phoneNumberId + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", numeroDestino,
                "type", "text",
                "text", Map.of("body", mensajeTexto)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("Mensaje enviado a {}: {}", numeroDestino, response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.error("Meta respondió {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error al enviar mensaje a WhatsApp", e);
        }
    }
}
