package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.SucursalDTO;
import com.mx.mitienda.model.dto.SucursalResponseDTO;
import com.mx.mitienda.service.ISucursalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final ISucursalService iSucursal;

    @Tag(name = "Sucursal SAVE", description = "Operaciones relacionadas con salvar sucursal")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SucursalResponseDTO>  create(@RequestBody SucursalDTO dto) {
        SucursalResponseDTO response =  iSucursal.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Tag(name = "Sucursal UPDATE", description = "Operaciones relacionadas con actualizar sucursal")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SucursalResponseDTO>  update(@PathVariable Long id, @RequestBody SucursalDTO dto) {
        return ResponseEntity.ok(iSucursal.update(id, dto));
    }

    @Tag(name = "Sucursal DISABLE", description = "Operaciones relacionadas con inhabilitar sucursal")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void>  disable(@PathVariable Long id){
        iSucursal.disable(id);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "Sucursal FIND ALL", description = "Operaciones relacionadas con encontrar todas las  sucursales")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<SucursalResponseDTO> >findAll(){
        return ResponseEntity.ok(iSucursal.findAll());
    }

    @Tag(name = "Sucursal FIND", description = "Operaciones relacionadas con encontrar una sola sucursal")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<SucursalResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(iSucursal.findById(id));
    }

    @GetMapping("/tipo-negocio/{businessTypeId}")
    public ResponseEntity<List<SucursalResponseDTO>> getByBusinessType(@PathVariable Long businessTypeId) {
        return ResponseEntity.ok(iSucursal.getByBusinessType(businessTypeId));
    }

    @PutMapping("/isCriticAlert/{sucursalId}")
    public ResponseEntity<SucursalResponseDTO> isCriticAlert(@PathVariable Long sucursalId,  @RequestParam(required =false) Boolean isEnable) {
        return ResponseEntity.ok(iSucursal.isStockCriticAlert(sucursalId, isEnable));
    }

}
