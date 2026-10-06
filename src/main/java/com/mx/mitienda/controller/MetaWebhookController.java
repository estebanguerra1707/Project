package com.mx.mitienda.controller;

import com.mx.mitienda.service.IBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/webhook")
@RequiredArgsConstructor
public class MetaWebhookController {

    private final IBotService botService;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        String VERIFY_TOKEN = "MI_TOKEN_SECRETO_123";
        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody String payload) {
        // Delegamos el procesamiento al servicio.
        // Se ejecuta rápido para devolver el 200 OK a Meta inmediatamente.
        botService.procesarPayloadMeta(payload);

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}