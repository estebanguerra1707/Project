package com.mx.mitienda.controller;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import com.mx.mitienda.service.IClienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Tag(name = "CLIENTES SAVE CLIENT", description = "Crea cliente por sucursal. Si existe inactivo, lo reactiva y actualiza. SUPER_ADMIN debe enviar branchId.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<ClienteResponseDTO> saveClient(@RequestBody ClienteDTO cliente) {
        return ResponseEntity.ok(clienteService.save(cliente));
    }

    @Tag(name = "CLIENTES GET ALL CLIENT", description = "Lista clientes por sucursal. SUPER_ADMIN puede indicar branchId como query param (?branchId=).")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public List<ClienteResponseDTO> getAll(@RequestParam(required = false) Long branchId) {
        // Si tu service actual NO acepta branchId, tienes 2 caminos:
        // 1) Crear un método getAllByBranchId(branchId) en service
        // 2) Reusar advancedSearch mandando un filtro con branchId
        //
        // Aquí aplico el camino 2 para no cambiar tu interfaz:
        if (branchId != null) {
            ClienteFiltroDTO filtro = new ClienteFiltroDTO();
            filtro.setBranchId(branchId);
            // si quieres, puedes setear active=true por default:
            // filtro.setActive(true);
            return clienteService.advancedSearch(filtro);
        }
        return clienteService.getAll();
    }

    @Tag(name = "CLIENTES GET CLIENT BY ID", description = "Obtiene cliente por id (valida pertenencia a sucursal según rol).")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<ClienteResponseDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getById(id));
    }

    @Tag(name = "CLIENTES UPDATE CLIENT", description = "Actualiza cliente. SUPER_ADMIN debe enviar branchId para decidir/crear relación con sucursal.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Long id, @RequestBody ClienteDTO cliente) {
        return ResponseEntity.ok(clienteService.updateClient(id, cliente));
    }

    @Tag(name = "CLIENTES DELETE CLIENT", description = "Deshabilita relación cliente-sucursal (borrado lógico por sucursal).")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public void disable(@PathVariable Long id) {
        clienteService.disableClient(id);
    }

    @Tag(name = "CLIENTES ADVANCED SEARCH CLIENT", description = "Búsqueda avanzada por sucursal. SUPER_ADMIN debe enviar branchId en el filtro.")
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> advancedClientSearch(@RequestBody ClienteFiltroDTO filtro) {
        return ResponseEntity.ok(clienteService.advancedSearch(filtro));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public Page<ClienteResponseDTO> getClientesPage(
            ClienteFiltroDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return clienteService.advancedSearchPage(filtro, pageable);
    }

}
