package com.mx.mitienda.controller;

import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.service.ProductoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/productos")
public class ProductController {

    private final ProductoService productService;

    public ProductController(ProductoService service) {
        this.productService = service;
    }
    @Tag(name = "PRODUCT SAVE", description = "Operaciones relacionadas con SALVAR PRODUCT")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ProductoResponseDTO save(@RequestBody ProductoDTO product) {
        return productService.save(product);
    }
    @Tag(name = "PRODUCT GET ALL", description = "Operaciones relacionadas con GET ALL PRODUCTS")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public List<ProductoResponseDTO> getAll() {
        return productService.getAll();
    }
    @Tag(name = "PRODUCT GET BY ID", description = "Operaciones relacionadas con GET BY ID PRODUCT")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ProductoResponseDTO getById(@PathVariable Long id) {
        return productService.getById(id);
    }
    @Tag(name = "PRODUCT UPDATE", description = "Operaciones relacionadas con UPDATE PRODUCT")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ProductoResponseDTO update(@PathVariable Long id, @RequestBody ProductoDTO productoDTO) {
        return productService.updateProduct(productoDTO, id);
    }
    @Tag(name = "PRODUCT DELETE", description = "Operaciones relacionadas con DELETE PRODUCT")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public void delete(@PathVariable Long id) {
        productService.disableProduct(id);
    }


}
