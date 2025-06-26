package com.mx.mitienda.controller;

import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCategory> createCategory(@RequestBody ProductCategoryDTO dto) {
        ProductCategory created = categoryService.save(dto);
        return ResponseEntity.ok(created);
    }
}