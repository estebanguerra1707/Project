package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.service.IProductoService;
import com.mx.mitienda.service.ProductoServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/productos")
public class ProductController {

    private final IProductoService productService;

    public ProductController(ProductoServiceImpl service) {
        this.productService = service;
    }
    @Tag(name = "PRODUCT SAVE", description = "Operaciones relacionadas con SALVAR PRODUCT")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ProductoResponseDTO> save(@RequestBody ProductoDTO product) {
        return ResponseEntity.ok(productService.save(product));
    }
    @Tag(name = "PRODUCT GET ALL", description = "Operaciones relacionadas con GET ALL PRODUCTS")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<ProductoResponseDTO>> getProducts() {
        return ResponseEntity.ok(productService.findCurrentUserProductos());
    }

    @Tag(name = "PRODUCT GET BY ID", description = "Operaciones relacionadas con GET BY ID PRODUCT")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ProductoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @Tag(name = "PRODUCT UPDATE", description = "Operaciones relacionadas con UPDATE PRODUCT")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id, @RequestBody ProductoDTO productoDTO) {
        return ResponseEntity.ok(productService.updateProduct(productoDTO, id));
    }

    @Tag(name = "PRODUCT DELETE", description = "Operaciones relacionadas con DELETE PRODUCT")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public void delete(@PathVariable Long id) {
        productService.disableProduct(id);
    }

    @GetMapping("/sucursal/{branchId}/tipo-negocio/{businessTypeId}")
    public ResponseEntity<List<ProductoResponseDTO>> getBySucursalAndTipo(
            @PathVariable Long branchId,
            @PathVariable Long businessTypeId
    ) {
        return ResponseEntity.ok(productService.findByBranchAndBusinessType(branchId, businessTypeId));
    }
}
