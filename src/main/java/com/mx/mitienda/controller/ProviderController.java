package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.ProveedorDTO;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.service.ProveedorServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/proveedores")
public class ProviderController {

    private final ProveedorServiceImpl proveedorService;

    public ProviderController(ProveedorServiceImpl ProveedorService) {
        this.proveedorService = ProveedorService;
    }
    @Tag(name = "PROVIDER SAVE", description = "Operaciones relacionadas con SAVE PROVIDER")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ProveedorResponseDTO save(@RequestBody ProveedorDTO Proveedor) {
        return proveedorService.save(Proveedor);
    }
    @Tag(name = "PROVIDER get all ", description = "Operaciones relacionadas con get all PROVIDER")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<ProveedorResponseDTO> getAll() {
        return proveedorService.getAll();
    }
    @Tag(name = "PROVIDER get by id ", description = "Operaciones relacionadas con get by id PROVIDER")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ProveedorResponseDTO getById(@PathVariable Long id) {
        return proveedorService.getById(id);
    }
    @Tag(name = "PROVIDER update ", description = "Operaciones relacionadas con update PROVIDER")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ProveedorResponseDTO update(@PathVariable Long id, @RequestBody ProveedorDTO proveedor) {
        return proveedorService.update(id, proveedor);
    }
    @Tag(name = "PROVIDER delete ", description = "Operaciones relacionadas con delete PROVIDER")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        proveedorService.disable(id);
    }
}
