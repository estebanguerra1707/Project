package com.mx.mitienda.controller;

import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CATEGORÍAS", description = "Operaciones sobre categorías de productos")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    public ProductCategoryController(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN)")
    public ResponseEntity<ProductCategoryResponseDTO> createCategory(@RequestBody ProductCategoryDTO dto) {
        ProductCategoryResponseDTO created = categoryService.save(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN)")
    public ResponseEntity<List<ProductCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAll());
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
        return ResponseEntity.ok(categoryService.getByCurrentUserBusinessType());
    }


}