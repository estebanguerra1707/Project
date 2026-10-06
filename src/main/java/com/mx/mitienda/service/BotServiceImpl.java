package com.mx.mitienda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.model.ReservaBot;
import com.mx.mitienda.repository.ReservaBotRepository;
import com.mx.mitienda.repository.InventarioSucursalRepository;

import com.mx.mitienda.service.IBotService;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotServiceImpl implements IBotService {

    private final ObjectMapper objectMapper;
    private final ReservaBotRepository reservaRepository;
    private final InventarioSucursalRepository inventarioRepository;
    private final IMetaApiClientService metaApiClient;

    @Override
    @Transactional
    public void procesarPayloadMeta(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            // Meta anida los mensajes profundamente. Esta es la ruta estándar para WhatsApp:
            JsonNode entry = root.path("entry").get(0);
            JsonNode changes = entry.path("changes").get(0);
            JsonNode value = changes.path("value");

            // Verificamos si realmente es un mensaje (y no un estado de "entregado" o "leído")
            if (value.has("messages")) {
                JsonNode messageInfo = value.path("messages").get(0);
                String senderPhone = messageInfo.path("from").asText();
                String textMessage = messageInfo.path("text").path("body").asText();

                log.info("Mensaje recibido de {}: {}", senderPhone, textMessage);

                // Aquí iría tu lógica conversacional (Ej: si el texto dice "Reservar")
                // Por ahora, simularemos la confirmación de una reserva:

                Long sucursalId = 1L; // Sucursal Principal
                Long productoId = 5L; // ID del producto/servicio

                // Horario de ejemplo: mañana de 10:00 a 11:00
                LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime fin = inicio.plusHours(1);

                crearReservaYDescontarInventario(sucursalId, productoId, inicio, fin, senderPhone);
            }
        } catch (Exception e) {
            log.error("Error procesando el webhook de Meta", e);
        }
    }
    // En tu método crearReservaYDescontarInventario, reemplaza los TODOs:
    private void crearReservaYDescontarInventario(Long sucursalId, Long productoId, LocalDateTime inicio, LocalDateTime fin, String telefono) {
        // 1. Validar solapamiento
        if (reservaRepository.existeSolapamiento(sucursalId, inicio, fin)) {
            log.warn("El horario ya está ocupado.");
            metaApiClient.enviarMensajeTexto(telefono, "Lo siento 😔, ese horario ya está ocupado. ¿Podrías elegir otro?");
            return;
        }

        // 2. Intentar descontar el stock
        int actualizados = inventarioRepository.descontarStockSeguro(productoId, sucursalId, BigDecimal.ONE, InventarioOwnerType.PROPIO);
        if (actualizados == 0) {
            log.warn("Sin stock suficiente.");
            metaApiClient.enviarMensajeTexto(telefono, "Una disculpa, nos hemos quedado sin stock para ese producto 📦.");
            return;
        }

        // 3. Crear y guardar
        ReservaBot reserva = new ReservaBot();
        reserva.setHoraInicio(inicio);
        reserva.setHoraFin(fin);
        reservaRepository.save(reserva);

        // ¡Aquí envías la confirmación real!
        String msjConfirmacion = String.format("¡Reserva confirmada! 🎉 Tu pedido quedó agendado para el día %s de %02d:00 a %02d:00.",
                inicio.toLocalDate().toString(), inicio.getHour(), fin.getHour());

        metaApiClient.enviarMensajeTexto(telefono, msjConfirmacion);
    }
}