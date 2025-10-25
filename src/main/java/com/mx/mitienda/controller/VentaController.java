package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.service.IDevolucionVentasService;
import com.mx.mitienda.service.IVentaService;
import com.mx.mitienda.service.VentaServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<VentaResponseDTO> registerSell(@RequestBody VentaRequestDTO ventaRequest, Authentication authentication){
        String username = authentication.getName();
        VentaResponseDTO venta = ventaServiceImpl.registerSell(ventaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }
    @Tag(name = "VENTA GET ALL", description = "Operaciones relacionadas con obtener todas las ventas ")

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<List<VentaResponseDTO>> getSales() {
        return ResponseEntity.ok(ventaServiceImpl.getAll());
    }

    @Tag(name = "VENTAS FILTER", description = "Operaciones relacionadas con registrar filtrar las ventas ")
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<List<VentaResponseDTO>>findByFilter(@RequestBody VentaFiltroDTO filter, Authentication authentication){
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream().findFirst().map(auth -> auth.getAuthority().replace("ROLE_","")).orElse("");
        List<VentaResponseDTO> ventaList = ventaServiceImpl.findByFilter(filter);
        return ResponseEntity.ok(ventaList);
    }

    @Tag(name = "VENTAS ID", description = "Operaciones relacionadas con obtener venta por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<VentaResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(ventaServiceImpl.getById(id));
    }

    @Tag(name = "VENTAS detalles", description = "Operaciones relacionadas con detalle venta")
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<List<DetalleVentaResponseDTO>> getDetallesPorVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaServiceImpl.getDetailsPerSale(id));
    }

    @GetMapping("/ganancias")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<ReporteGananciasDTO> obtenerResumen() {
        return ResponseEntity.ok(new ReporteGananciasDTO(
                ventaServiceImpl.obtenerGananciaHoy(),
                ventaServiceImpl.obtenerGananciaSemana(),
                ventaServiceImpl.obtenerGananciaMes()
        ));
    }

    @GetMapping("/ganancia-dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<GanaciaDiaDTO> getGananciaDia(@RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        BigDecimal ganancia = ventaServiceImpl.obtenerGananciaPorDia(fecha);
        return ResponseEntity.ok(new GanaciaDiaDTO(fecha, ganancia));
    }


    @PostMapping("/ganancia-rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<BigDecimal> getGananciaPorRango(@RequestBody GananciaPorFechaDTO gananciaPorFechaDTO) {
        return ResponseEntity.ok(ventaServiceImpl.obtenerGananciaPorRango(gananciaPorFechaDTO.getStartDate(), gananciaPorFechaDTO.getEndDate()));
    }
    @PostMapping("/ganancia-diaria-rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<List<GanaciaDiaDTO>> getGananciaDiariaRango(@RequestBody GananciaPorFechaDTO gananciaPorFechaDTO) {
        Map<LocalDate, BigDecimal> datos = ventaServiceImpl.obtenerGananciasPorDiaEnRango(gananciaPorFechaDTO.getStartDate(), gananciaPorFechaDTO.getEndDate());

        List<GanaciaDiaDTO> respuesta = datos.entrySet().stream()
                .map(entry -> new GanaciaDiaDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }


    // 1) Ganancia (neta) por venta específica (considerando devoluciones)
    @GetMapping("/ganancia/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<BigDecimal> obtenerGananciaPorVenta(@PathVariable Long ventaId) {
        BigDecimal ganancia = ventaServiceImpl.obtenerGananciaPorVenta(ventaId);
        return ResponseEntity.ok(ganancia);
    }

    // 2) Ventas BRUTAS por rango [desde, hasta]
    @GetMapping("/brutas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<BigDecimal> obtenerVentasBrutasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        BigDecimal total = ventaServiceImpl.obtenerVentasBrutasPorRango(desde, hasta);
        return ResponseEntity.ok(total);
    }

    // 3) Ventas NETAS por rango [desde, hasta] (resta importes devueltos)
    @GetMapping("/netas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<BigDecimal> obtenerVentasNetasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        BigDecimal total = ventaServiceImpl.obtenerVentasNetasPorRango(desde, hasta);
        return ResponseEntity.ok(total);
    }


    @PostMapping("/devolucion")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<DevolucionVentasResponseDTO> devolverVenta(
            @Valid @RequestBody DevolucionVentasRequestDTO devolucionVentasRequestDTO,
            Authentication authentication) {
        return ResponseEntity.ok(devolucionVentasService.procesarDevolucion(devolucionVentasRequestDTO, authentication));
    }

    @GetMapping(
            value = "/{ventaId}/ticket",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_USER')")
    public ResponseEntity<byte[]> getTicketVenta(@PathVariable Long ventaId) {

        byte[] pdf = ventaServiceImpl.generateTicketPdf(ventaId);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=ticket.pdf")
                .body(pdf);
    }


}
