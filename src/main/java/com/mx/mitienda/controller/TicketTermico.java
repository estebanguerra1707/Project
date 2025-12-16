package com.mx.mitienda.controller;

import com.mx.mitienda.service.TicketTermicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket-raw")
@RequiredArgsConstructor
public class TicketTermico {
    private final TicketTermicoService ticketTermicoService;

    @GetMapping("/{type}/{id}")
    public ResponseEntity<String> getRawTicket(
            @PathVariable String type,
            @PathVariable Long id
    ) {
        String raw = ticketTermicoService.generateRawTicket(type, id);
        return ResponseEntity.ok(raw);
    }

}
