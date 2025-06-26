package com.mx.mitienda.controller;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.service.ClienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }
    @Tag(name = "CLIENTES SAVE CLIENT", description = "Operaciones relacionadas con salvar clientes")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public Cliente saveClient(@RequestBody Cliente cliente) {
        return clienteService.save(cliente);
    }

    @Tag(name = "CLIENTES GET ALL CLIENT", description = "Operaciones relacionadas con GET ALL clientes")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public List<Cliente> getAll() {
        return clienteService.getAll();
    }
    @Tag(name = "CLIENTES GET CLIENT BY ID", description = "Operaciones relacionadas con GET CLIENT BY ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public Cliente getClientById(@PathVariable Long id) {
        return clienteService.getById(id);
    }

    @Tag(name = "CLIENTES UPDATE CLIENT", description = "Operaciones relacionadas con UPDATE clientes")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public Cliente update(@PathVariable Long id, @RequestBody ClienteDTO cliente) {
        Cliente updated = clienteService.updateClient(id, cliente);
        return ResponseEntity.ok(updated).getBody();
    }

    @Tag(name = "CLIENTES DELETE CLIENT", description = "Operaciones relacionadas con DELETE clientes")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public void disable(@PathVariable Long id) {
        clienteService.disableClient(id);
    }
    @Tag(name = "CLIENTES ADVANCED SEARCH CLIENT", description = "Operaciones relacionadas con ADVANCE SEARCH client")

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public List<Cliente> advancedClientSearch(@RequestBody ClienteFiltroDTO filtro) {
        return clienteService.advancedSearch(filtro);
    }

}
