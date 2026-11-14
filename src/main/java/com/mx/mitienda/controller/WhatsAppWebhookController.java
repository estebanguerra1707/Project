package com.mx.mitienda.controller;

import com.mx.mitienda.service.IWhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final String VERIFY_TOKEN = "rellenitos_secure_token_2026";
    private final IWhatsAppService whatsAppService;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token de verificación inválido");
    }

    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody String payload) {
        // Delegamos el procesamiento pesado al servicio de arquitectura limpia
        whatsAppService.processIncomingMessage(payload);
        return ResponseEntity.ok().build();
    }
}