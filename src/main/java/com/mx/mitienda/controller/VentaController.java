package com.mx.mitienda.controller;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.service.VentaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @Tag(name = "VENTAS registrar venta", description = "Operaciones relacionadas con registrar todas las ventas ")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Venta> registerSell(@RequestBody VentaRequestDTO ventaRequest, Authentication authentication){
        String username = authentication.getName();
        Venta venta = ventaService.registerSell(ventaRequest, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }
    @Tag(name = "VENTA GET ALL", description = "Operaciones relacionadas con obtener todas las ventas ")

    @GetMapping
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<Venta>> getAll(Authentication authentication){
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream().findFirst().map(auth -> auth.getAuthority().replace("ROLE_","")).orElse("");
        List<Venta> listSell = ventaService.getAll(username, role);
        return ResponseEntity.ok(listSell);
    }

    @Tag(name = "VENTAS FILTER", description = "Operaciones relacionadas con registrar filtrar las ventas ")
    @PostMapping("/filter")
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<Venta>>findByFilter(@RequestBody VentaFiltroDTO filter, Authentication authentication){
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream().findFirst().map(auth -> auth.getAuthority().replace("ROLE_","")).orElse("");
        List<Venta> ventaList = ventaService.findByFilter(filter, username, role);
        return ResponseEntity.ok(ventaList);
    }

    @Tag(name = "VENTAS ID", description = "Operaciones relacionadas con obtener venta por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Venta> getById(@PathVariable Long id){
        return ResponseEntity.ok(ventaService.getById(id));
    }

    @Tag(name = "VENTAS detalles", description = "Operaciones relacionadas con detalle venta")
    @GetMapping("/{id}/detalles")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<DetalleVenta>> getDetallesPorVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.getDetailsPerSale(id));
    }

    @Tag(name = "VENTAS ID DELETE", description = "Operaciones relacionadas con delete venta")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        ventaService.deleteSell(id);
        return ResponseEntity.noContent().build();
    }


}
