package com.mx.mitienda.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiAiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

    public GeminiAiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode parseOrderFromChat(List<String> chatHistory) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";
        // 1. Definimos el "System Prompt" con las reglas de Los Rellenitos


        String systemInstruction = """
            Eres un asistente virtual para 'Chilaquiles Los Rellenitos by AriEs Kitchen'.
            Tu objetivo es leer el historial de chat con el cliente y extraer el pedido en formato JSON estricto.
            
            REGLAS DEL MENÚ:
            - Tipos: Tradicionales o Rellenitos.
            - Salsas: Verde o Roja.
            - Proteínas comunes: Bistec, Pollo, Huevo (estrellado, revuelto, cocido de ambos lados).
            - Base (siempre va incluida): Cebolla, queso y crema.
            
            ESTRUCTURA JSON REQUERIDA:
            {
              "es_pedido_completo": boolean, (true si ya pidió su comida, false si solo está saludando)
              "falta_ubicacion": boolean, (true si el cliente quiere envío pero no ha mandado su ubicación)
              "productos": [
                {
                  "tipo": "Tradicionales/Rellenitos",
                  "salsa": "Verde/Roja",
                  "proteina": "Pollo/Bistec/Huevo/Sencillos",
                  "modificaciones": "Ej: Sin cebolla, huevo cocido de ambos lados"
                }
              ]
            }
            Devuelve ÚNICAMENTE el JSON, sin formato markdown ni texto extra.
            """;

        // Unimos el historial de chat en un solo bloque de texto
        String userChat = String.join("\n", chatHistory);

        // 2. Construimos el payload para la API de Gemini
        Map<String, Object> body = new HashMap<>();

        // Configuramos las instrucciones del sistema
        body.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemInstruction))
        ));

// Configuramos el mensaje del usuario (el historial)
        body.put("contents", List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", "Historial del cliente:\n" + userChat))
                )
        ));

        body.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.1
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        var request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                log.warn("Gemini devolvió respuesta sin texto: {}", response.getBody());
                return objectMapper.createObjectNode();
            }

            String json = textNode.asText()
                    .replaceAll("(?s)^```(?:json)?\\s*|\\s*```$", "")
                    .trim();

            return objectMapper.readTree(json);

        }  catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("Gemini respondió {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return objectMapper.createObjectNode();
        } catch (Exception e) {
            log.error("Error procesando con Gemini", e);
            return objectMapper.createObjectNode();
        }
    }
}