package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.service.IDevolucionVentasService;
import com.mx.mitienda.service.IVentaService;
import com.mx.mitienda.service.VentaServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final IVentaService ventaServiceImpl;
    private final IDevolucionVentasService devolucionVentasService;


    @Tag(name = "VENTAS registrar venta", description = "Operaciones relacionadas con registrar todas las ventas ")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<VentaResponseDTO> registerSell(@RequestBody VentaRequestDTO ventaRequest, Authentication authentication){
        String username = authentication.getName();
        VentaResponseDTO venta = ventaServiceImpl.registerSell(ventaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }
    @Tag(name = "VENTA GET ALL", description = "Operaciones relacionadas con obtener todas las ventas ")

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<VentaResponseDTO>> getSales() {
        return ResponseEntity.ok(ventaServiceImpl.getAll());
    }

    @Tag(name = "VENTAS FILTER", description = "Operaciones relacionadas con registrar filtrar las ventas ")
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<Page<VentaResponseDTO>>findByFilter(  @RequestBody VentaFiltroDTO filter,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size){
        Page<VentaResponseDTO> result = ventaServiceImpl.findByFilter(filter, page, size);
        return ResponseEntity.ok(result);
    }

    @Tag(name = "VENTAS ID", description = "Operaciones relacionadas con obtener venta por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<VentaResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(ventaServiceImpl.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteVenta(@PathVariable Long id) {
        ventaServiceImpl.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "VENTAS detalles", description = "Operaciones relacionadas con detalle venta")
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<DetalleVentaResponseDTO>> getDetallesPorVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaServiceImpl.getDetailsPerSale(id));
    }



    @PostMapping("/devolucion")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<DevolucionVentasResponseDTO> devolverVenta(
            @Valid @RequestBody DevolucionVentasRequestDTO devolucionVentasRequestDTO) {
        return ResponseEntity.ok(devolucionVentasService.procesarDevolucion(devolucionVentasRequestDTO));
    }

    @Tag(name = "DEVOLUCIONES FILTER", description = "Filtrar devoluciones de ventas")
    @PostMapping("/filterDevoluciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<Page<FiltroDevolucionVentasResponseDTO>> filterDevoluciones(
            @Valid @RequestBody DevolucionesVentasFiltroDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<FiltroDevolucionVentasResponseDTO> result =
                devolucionVentasService.findByFilter(filter, page, size);

        return ResponseEntity.ok(result);
    }
    @GetMapping(
            value = "/{ventaId}/ticket",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> getTicketVenta(@PathVariable Long ventaId) {

        byte[] pdf = ventaServiceImpl.generateTicketPdf(ventaId);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=ticket.pdf")
                .body(pdf);
    }

}
