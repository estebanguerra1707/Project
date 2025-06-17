package com.mx.mitienda.controller;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Compra> save(@RequestBody Compra compra, Authentication authentication) {
        String username = authentication.getName(); // <-- viene del token
        Compra saved = compraService.save(compra, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<Compra>> getAll(Authentication authentication) {
        String username = authentication.getName();
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(granted -> granted.getAuthority().replace("ROLE_", ""))
                .orElse("");

        List<Compra> compras = compraService.getAll(username, rol);
        return ResponseEntity.ok(compras);
    }
}
