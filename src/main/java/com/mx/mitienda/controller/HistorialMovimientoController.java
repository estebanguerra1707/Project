package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.HistorialMovimientosResponseDTO;
import com.mx.mitienda.service.IHistorialMovimientosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historial")
@RequiredArgsConstructor
public class HistorialMovimientoController {

    private final IHistorialMovimientosService historialMovimientosService;
    @GetMapping("/producto/{productoId}")
    public Page<HistorialMovimientosResponseDTO> historialPaginadoPorProducto(
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return historialMovimientosService.obtenerPaginadoPorProducto(productoId, PageRequest.of(page, size));
    }

    @GetMapping("/inventario/{inventarioId}")
    public Page<HistorialMovimientosResponseDTO> historialPaginadoPorInventario(
            @PathVariable Long inventarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return historialMovimientosService.obtenerPaginadoPorInventario(inventarioId, PageRequest.of(page, size));
    }
}
