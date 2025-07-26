package com.mx.mitienda.controller;

import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;
import com.mx.mitienda.model.dto.MovimientoStockDTO;
import com.mx.mitienda.service.IInventarioSucursalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/inventario")
@Tag(name = "INVENTARIO EN SUCURSALES", description = "Operaciones sobre inventario en sucursales")
@RequiredArgsConstructor
public class InventarioSucursalController {

    private final IInventarioSucursalService inventarioSucursalService;

    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN')")
    public  ResponseEntity<List<InventarioSucursalResponseDTO>> getInventarioSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(inventarioSucursalService.getProductosEnSucursal(sucursalId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventarioSucursalResponseDTO> actualizarInventario(
            @PathVariable Long id,
            @RequestBody InventarioSucursalRequestDTO dto
    ) {
        InventarioSucursalResponseDTO actualizado = inventarioSucursalService.actualizarInventario(id, dto);
        return ResponseEntity.ok(actualizado);
    }


    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN')")
    public  ResponseEntity<List<InventarioSucursalResponseDTO>> getInventarioProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioSucursalService.getProducto(productoId));
    }

    @GetMapping("/sucursal/{sucursalId}/producto/{productId}")
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN')")
    public  ResponseEntity<List<InventarioSucursalResponseDTO>> getInventarioProductoSucursal(@PathVariable Long sucursalId, @PathVariable Long productId) {
        return ResponseEntity.ok(inventarioSucursalService.getProductoEnSucursal(sucursalId, productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventarioSucursalResponseDTO> createInventario(@RequestBody InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        return ResponseEntity.ok(inventarioSucursalService.create(inventarioSucursalRequestDTO));
    }

    @PostMapping("/aumentar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> aumentarStock(@RequestBody MovimientoStockDTO movimientoStockDTO){
        inventarioSucursalService.aumentarStock(movimientoStockDTO.getProductId(), movimientoStockDTO.getCantidad());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/disminuir")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> disminuirStock(@RequestBody MovimientoStockDTO movimiento) {
        inventarioSucursalService.disminuirStock(movimiento.getProductId(), movimiento.getCantidad());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public List<InventarioSucursalResponseDTO> getByBusinessType() {
        return inventarioSucursalService.findByBranchAndBusinessType();
    }
}
