package com.mx.mitienda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.client.GeminiAiClient;
import com.mx.mitienda.client.GoogleMapsDistanceClient;
import com.mx.mitienda.client.WhatsAppApiClient;
import com.mx.mitienda.config.DeliveryProperties;
import com.mx.mitienda.util.GeoUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class WhatsAppServiceImpl implements IWhatsAppService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(WhatsAppServiceImpl.class);

    private final Set<String> processedIds = Collections.synchronizedSet(
            Collections.newSetFromMap(new LinkedHashMap<String, Boolean>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 1000;
                }
            }));

    private final GoogleMapsDistanceClient distanceClient;
    private final GeminiAiClient geminiAiClient;
    private final ChatSessionService chatSessionService;
    private final WhatsAppApiClient whatsAppApiClient;
    private final DeliveryProperties deliveryProperties;
    private final IWhatsAppSenderService whatsAppSender;

    @Override
    @Async
    public void processIncomingMessage(String payload) {
        try {
            JsonNode value = objectMapper.readTree(payload).at("/entry/0/changes/0/value");
            JsonNode message = value.at("/messages/0");

            // Meta también manda webhooks de estado (entregado/leído) sin "messages"
            if (message.isMissingNode()) {
                return;
            }

            String id = message.path("id").asText();
            if (!processedIds.add(id)) {
                log.info("Mensaje duplicado ignorado: {}", id);
                return;
            }

            String from = message.path("from").asText();
            String type = message.path("type").asText();

            if ("text".equals(type)) {
                handleTextMessage(from, message.path("text").path("body").asText());
            } else if ("location".equals(type)) {
                JsonNode location = message.path("location");
                handleLocationMessage(from,
                        location.path("latitude").asDouble(),
                        location.path("longitude").asDouble());
            }
        } catch (Exception e) {
            log.error("Error procesando el payload de WhatsApp", e);
        }

    }

    private void handleTextMessage(String from, String textBody) {
        // 1. Guardamos el mensaje del cliente en su historial temporal
        chatSessionService.addMessage(from, "Cliente", textBody);

        // 2. Obtenemos todo el historial de la conversación
        List<String> history = chatSessionService.getHistory(from);

        // 3. Consultamos a Gemini para que analice si el pedido está completo
        JsonNode analysis = geminiAiClient.parseOrderFromChat(history);

        boolean pedidoCompleto = analysis.path("es_pedido_completo").asBoolean(false);
        boolean faltaUbicacion = analysis.path("falta_ubicacion").asBoolean(true);

        if (pedidoCompleto && faltaUbicacion) {
            // La IA detectó que ya sabe qué quiere comer, pero le falta la ubicación
            String respuesta = "¡Perfecto! Me podrías mandar tu ubicación por favor para que el repartidor llegue directo 🙏🏼";
            whatsAppSender.enviarRespuestaWhatsApp(from, respuesta);
            chatSessionService.addMessage(from, "Bot", respuesta);
        } else if (!pedidoCompleto) {
            // Siguen platicando o aclarando dudas (ej. cambio de proteína)
            // Puedes dejar que la IA responda o mandar un mensaje de seguimiento humano/amable.
            System.out.println("El cliente sigue en proceso de selección.");
        }
    }

    private void handleLocationMessage(String from, double lat, double lon) {
        chatSessionService.addMessage(from, "Cliente", "[El cliente envió su ubicación]");

        double maxKm = deliveryProperties.maxKm();

        // Filtro rápido: si ni en línea recta llega, no consultamos Google
        double straightKm = GeoUtils.haversineKm(
                deliveryProperties.originLat(), deliveryProperties.originLon(), lat, lon);
        if (straightKm > maxKm) {
            sendOutOfCoverage(from);
            return;
        }

        // Confirmación con distancia real por ruta
        double distanceKm = distanceClient.calculateDistanceInKm(lat, lon);
        if (distanceKm < 0 || distanceKm > maxKm) {
            sendOutOfCoverage(from);
            return;
        }

        String respuesta = String.format(
                "Súper! Sí tenemos cobertura 🤩. La distancia es de %.1f km.", distanceKm);
        whatsAppSender.enviarRespuestaWhatsApp(from, respuesta);
        chatSessionService.addMessage(from, "Bot", respuesta);
    }

    private void sendOutOfCoverage(String from) {
        String msg = "Lo sentimos, por el momento estamos fuera de cobertura para esa ubicación 🛵💨";
        whatsAppSender.enviarRespuestaWhatsApp(from, msg);
        chatSessionService.addMessage(from, "Bot", msg);
    }
}