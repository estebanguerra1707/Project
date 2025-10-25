package com.mx.mitienda.controller;

import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CATEGORÍAS", description = "Operaciones sobre categorías de productos")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN)")
    public ResponseEntity<ProductCategoryResponseDTO> createCategory(@RequestBody ProductCategoryDTO dto) {
        ProductCategoryResponseDTO created = categoryService.save(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','VENDOR')")
    public Page<ProductCategoryResponseDTO> getAll(
            ProductCategoryFiltroDTO filtro,           // query params: businessTypeId, nombre
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return categoryService.getAll(filtro, pageable);
    }

    @GetMapping("/business-type/{businessTypeId}")
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN)")
    public ResponseEntity<List<ProductCategoryResponseDTO>> getCategoriesByBusinessType(@PathVariable Long businessTypeId) {
        return ResponseEntity.ok(categoryService.getByBusinessType(businessTypeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductCategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }
    @Tag(name = "PRODUCT UPDATE", description = "Operaciones relacionadas con UPDATE PRODUCT")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductCategoryResponseDTO>  update(@PathVariable Long id, @RequestBody ProductCategoryDTO productCategoryDTO) {
        return ResponseEntity.ok(categoryService.update(id, productCategoryDTO));
    }
    @GetMapping("/actual")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<ProductCategoryResponseDTO>> getCategoriasActuales() {
        return ResponseEntity.ok(categoryService.getActualCatalog());
    }

    @GetMapping("/tipo-negocio/{businessTypeId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public List<ProductCategoryResponseDTO> byBusinessTypeAsList(@PathVariable Long businessTypeId) {
        return categoryService.findByBusinessTypeAsList(businessTypeId);
    }

}