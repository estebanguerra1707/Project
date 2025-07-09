package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.BusinessTypeDTO;
import com.mx.mitienda.model.dto.BusinessTypeResponseDTO;
import com.mx.mitienda.service.IBusinessTypeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/business-types")
@RequiredArgsConstructor
public class BusinessTypeController {

    private final IBusinessTypeService businessTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessTypeResponseDTO> create(@RequestBody BusinessTypeDTO dto) {
        return ResponseEntity.ok(businessTypeService.save(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BusinessTypeResponseDTO>> getAll() {
        return ResponseEntity.ok(businessTypeService.getAllOrderById());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        businessTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessTypeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(businessTypeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessTypeResponseDTO> update(
            @PathVariable Long id,
            @RequestBody BusinessTypeDTO businessTypeDTO) {
        return ResponseEntity.ok(businessTypeService.update(id, businessTypeDTO));
    }
}