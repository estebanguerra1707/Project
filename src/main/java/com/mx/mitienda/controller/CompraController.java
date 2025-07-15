package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.service.CompraServiceImpl;
import com.mx.mitienda.service.ICompraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    @Autowired
    private ICompraService compraServiceImpl;

    @Tag(name = "COMPRA SAVE", description = "Operaciones relacionadas con SALVAR compra ")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<CompraResponseDTO> save(@RequestBody CompraRequestDTO compra, Authentication authentication) {
        String username = authentication.getName(); // <-- viene del token
        CompraResponseDTO saved = compraServiceImpl.save(compra, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Tag(name = "COMPRA GET ALL", description = "Operaciones relacionadas con obtener todas las compras ")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<CompraResponseDTO>> getPurchases() {
        return ResponseEntity.ok(compraServiceImpl.findCurrentUserCompras());
    }

    @Tag(name = "COMPRA GET ALL", description = "Operaciones relacionadas con obtener todas las compras ")
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<CompraResponseDTO>> search(@RequestBody CompraFiltroDTO filtro) {
        return ResponseEntity.ok(compraServiceImpl.advancedSearch(filtro));
    }
}
