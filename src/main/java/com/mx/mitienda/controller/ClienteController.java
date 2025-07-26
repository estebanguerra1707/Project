package com.mx.mitienda.controller;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import com.mx.mitienda.service.IClienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController {

    private final IClienteService clienteService;

    @Tag(name = "CLIENTES SAVE CLIENT", description = "Operaciones relacionadas con salvar clientes")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public ResponseEntity<ClienteResponseDTO> saveClient(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.save(cliente));
    }

    @Tag(name = "CLIENTES GET ALL CLIENT", description = "Operaciones relacionadas con GET ALL clientes")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public List<ClienteResponseDTO> getAll() {
        return clienteService.getAll();
    }
    @Tag(name = "CLIENTES GET CLIENT BY ID", description = "Operaciones relacionadas con GET CLIENT BY ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getById(id));
    }

    @Tag(name = "CLIENTES UPDATE CLIENT", description = "Operaciones relacionadas con UPDATE clientes")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Long id, @RequestBody ClienteDTO cliente) {
        ClienteResponseDTO updated = clienteService.updateClient(id, cliente);
        return ResponseEntity.ok(updated);
    }

    @Tag(name = "CLIENTES DELETE CLIENT", description = "Operaciones relacionadas con DELETE clientes")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public void disable(@PathVariable Long id) {
        clienteService.disableClient(id);
    }
    @Tag(name = "CLIENTES ADVANCED SEARCH CLIENT", description = "Operaciones relacionadas con ADVANCE SEARCH client")

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public ResponseEntity<List<ClienteResponseDTO>> advancedClientSearch(@RequestBody ClienteFiltroDTO filtro) {
        return  ResponseEntity.ok(clienteService.advancedSearch(filtro));
    }

}
