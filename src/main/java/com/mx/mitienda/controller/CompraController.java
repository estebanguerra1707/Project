package com.mx.mitienda.controller;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.service.CompraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Tag(name = "COMPRA SAVE", description = "Operaciones relacionadas con SALVAR compra ")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Compra> save(@RequestBody Compra compra, Authentication authentication) {
        String username = authentication.getName(); // <-- viene del token
        Compra saved = compraService.save(compra, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Tag(name = "COMPRA GET ALL", description = "Operaciones relacionadas con obtener todas las compras ")
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
