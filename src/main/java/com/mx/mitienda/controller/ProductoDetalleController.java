package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.ProductoDetailDTO;
import com.mx.mitienda.model.dto.ProductoDetailResponseDTO;
import com.mx.mitienda.service.IProductDetailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/producto-detalle")
public class ProductoDetalleController {

    private final IProductDetailService productDetailService;

    @PostMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoDetailResponseDTO> createProductDetail(
            @PathVariable Long productId,
            @RequestBody ProductoDetailDTO productoDetailDTO) {
        ProductoDetailResponseDTO response = productDetailService.save(productId, productoDetailDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoDetailResponseDTO> getProductDetail(@PathVariable Long productId){
        ProductoDetailResponseDTO response = productDetailService.getProductDetail(productId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{detailId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoDetailResponseDTO> updatePruductDetail(
            @PathVariable Long detailId,
            @RequestBody ProductoDetailDTO productoDetailDTO
    ){
        ProductoDetailResponseDTO productoDetailResponseDTO = productDetailService.update(detailId, productoDetailDTO);
        return ResponseEntity.ok(productoDetailResponseDTO);
    }


}
