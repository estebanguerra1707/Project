package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.GastoRequestDTO;
import com.mx.mitienda.model.dto.GastoResponseDTO;
import com.mx.mitienda.service.IGastoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gastos")
@CrossOrigin(origins = "*") // Ajusta esto según los CORS de tu proyecto React
public class GastoController {

    private final IGastoService gastoService;

    public GastoController(IGastoService gastoService) {
        this.gastoService = gastoService;
    }

    @PostMapping
    public ResponseEntity<GastoResponseDTO> crearGasto(@RequestBody GastoRequestDTO requestDTO) {
        GastoResponseDTO nuevoGasto = gastoService.crearGasto(requestDTO);
        return new ResponseEntity<>(nuevoGasto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<GastoResponseDTO>> listarGastos(
            @PageableDefault(size = 10, sort = "fechaGasto", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(gastoService.obtenerTodosLosGastos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> obtenerGasto(@PathVariable Long id) {
        return ResponseEntity.ok(gastoService.obtenerGastoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> actualizarGasto(@PathVariable Long id, @RequestBody GastoRequestDTO requestDTO) {
        return ResponseEntity.ok(gastoService.actualizarGasto(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable Long id) {
        gastoService.eliminarGasto(id);
        return ResponseEntity.noContent().build();
    }
}