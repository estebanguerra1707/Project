package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.service.IAuthenticatedUserService;
import com.mx.mitienda.service.IProductoService;
import com.mx.mitienda.service.ProductoServiceImpl;
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

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductController {

    private final IProductoService productService;
    private final IAuthenticatedUserService authenticatedUserService;

    @Tag(name = "PRODUCT SAVE", description = "Operaciones relacionadas con SALVAR PRODUCT")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoResponseDTO> save(@RequestBody ProductoDTO product) {
        return ResponseEntity.ok(productService.save(product));
    }


    @Tag(name = "PRODUCT GET BY ID", description = "Operaciones relacionadas con GET BY ID PRODUCT")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @Tag(name = "PRODUCT GET ALL", description = "Operaciones relacionadas con GET ALL PRODUCTS")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public Page<ProductoResponseDTO> getAll(
            ProductoFiltroDTO filtro,
            @PageableDefault(size = 20, sort = "product.name") Pageable pageable
    ) {
        return productService.getAll(filtro, pageable);
    }

    @Tag(name = "PRODUCT UPDATE", description = "Operaciones relacionadas con UPDATE PRODUCT")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id, @RequestBody ProductoDTO productoDTO) {
        return ResponseEntity.ok(productService.updateProduct(productoDTO, id));
    }

    @Tag(name = "PRODUCT DELETE", description = "Operaciones relacionadas con DELETE PRODUCT")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        productService.disableProduct(id);
    }


    @GetMapping("/codigo-barras/{codigoBarras}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<ProductoResponseDTO> buscarPorCodigoBarras(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(productService.buscarPorCodigoBarras(codigoBarras));
    }

    @PostMapping("/avanzado")
    public Page<ProductoResponseDTO> buscarAvanzado(
            @RequestBody ProductoFiltroDTO filtro,
            @PageableDefault(size = 20) Pageable pageable // default; se puede sobreescribir con query params
    ) {
        // Usuario normal: siempre “forzar” su branch
        if (!authenticatedUserService.isSuperAdmin()) {
            filtro.setBranchId(authenticatedUserService.getCurrentBranchId());
        }
        // SUPER_ADMIN: si no manda branchId, se busca en TODAS las sucursales (branchId = null).
        // Puede además mandar businessTypeId para acotar por tipo de negocio.
        return productService.buscarAvanzado(filtro, pageable);
    }

    @Tag(name = "PRODUCTS BY BRANCH", description = "Obtiene productos filtrados por sucursal")
    @GetMapping("/by-branch/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<ProductoResponseDTO>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(productService.getProductsByBranch(branchId));
    }
}
