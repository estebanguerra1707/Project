package com.mx.mitienda.controller;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequest;
import com.mx.mitienda.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Venta> registerSell(@RequestBody VentaRequest ventaRequest, Authentication authentication){
        String username = authentication.getName();
        Venta venta = ventaService.registerSell(ventaRequest, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<Venta>> getAll(Authentication authentication){
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream().findFirst().map(auth -> auth.getAuthority().replace("ROLE_","")).orElse("");
        List<Venta> listSell = ventaService.getAll(username, role);
        return ResponseEntity.ok(listSell);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<Venta>>findByFilter(@RequestBody VentaFiltroDTO filter, Authentication authentication){
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream().findFirst().map(auth -> auth.getAuthority().replace("ROLE_","")).orElse("");
        List<Venta> ventaList = ventaService.findByFilter(filter, username, role);
        return ResponseEntity.ok(ventaList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Venta> getById(@PathVariable Long id){
        return ResponseEntity.ok(ventaService.getById(id));
    }

    @GetMapping("/{id}/detalles")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<List<DetalleVenta>> getDetallesPorVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.getDetailsPerSale(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        ventaService.deleteSell(id);
        return ResponseEntity.noContent().build();
    }


}
