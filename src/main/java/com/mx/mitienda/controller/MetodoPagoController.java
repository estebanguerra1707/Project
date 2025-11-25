package com.mx.mitienda.controller;


import com.mx.mitienda.model.dto.PaymentMethodDTO;
import com.mx.mitienda.model.dto.PaymentMethodResponseDTO;
import com.mx.mitienda.service.IMetodoPagoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/metodo-pago")
@RequiredArgsConstructor
public class MetodoPagoController {
    private final IMetodoPagoService paymentMethodService;

    @Tag(name = "PAYMENT METHODS", description = "Operaciones relacionadas con métodos de pago")

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<List<PaymentMethodResponseDTO>> getAll() {
        return ResponseEntity.ok(paymentMethodService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<PaymentMethodResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PaymentMethodResponseDTO> create(@RequestBody PaymentMethodDTO dto) {
        return ResponseEntity.ok(paymentMethodService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PaymentMethodResponseDTO> update(
            @PathVariable Long id,
            @RequestBody PaymentMethodDTO dto
    ) {
        return ResponseEntity.ok(paymentMethodService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
