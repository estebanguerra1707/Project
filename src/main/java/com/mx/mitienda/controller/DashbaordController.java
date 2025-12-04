package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.service.IDashboardService;
import com.mx.mitienda.service.IVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class DashbaordController {

    private final IDashboardService dashboardService;
    private final IVentaService ventaServiceImpl;

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public ResponseEntity<List<TopProductoDTO>> getTopProducts(
            @RequestParam String groupBy,
            @RequestParam(required = false) Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime end){
        List<TopProductoDTO> responseList = dashboardService.getTopProductos(groupBy, start, end, branchId);
        return ResponseEntity.ok(responseList);
    }


    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<DashboardResumenDTO> getResumen(
            @RequestParam(required = false) Long branchId
    ) {
        DashboardResumenDTO dto = dashboardService.obtenerResumen(branchId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/dashboard/top/semana")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public List<TopProductoDTO> topSemana(
            @RequestParam(required = false) Long branchId
    ) {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();

        return dashboardService.topVendidos(inicio, fin, branchId);
    }

    @GetMapping("/dashboard/top/mes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public List<TopProductoDTO> topMes(
            @RequestParam(required = false) Long branchId
    ) {
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        return dashboardService.topVendidos(inicio, fin, branchId);
    }

    @GetMapping("/top-vendidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<TopProductoDTO>> topVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) Long branchId
    ) {
        List<TopProductoDTO> lista = dashboardService.topVendidos(inicio, fin, branchId);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/top/consolidado")
    public List<TopProductoDTO> topMasVendidos(@RequestParam Long branchId) {
        return dashboardService.topVendidos(LocalDate.now().minusDays(30), LocalDate.now(), branchId);
    }

    @GetMapping("/top/por-usuario")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<TopProductoDTO> topPorUsuario(@RequestParam Long branchId) {
        return dashboardService.topVendidosPorUsuario(LocalDate.now().minusDays(30), LocalDate.now(), branchId);
    }

    @GetMapping("/ganancias")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ReporteGananciasDTO> obtenerResumen(Long branchId) {
        return ResponseEntity.ok(new ReporteGananciasDTO(
                ventaServiceImpl.obtenerGananciaHoy(branchId),
                ventaServiceImpl.obtenerGananciaSemana(branchId),
                ventaServiceImpl.obtenerGananciaMes(branchId)
        ));
    }

    @GetMapping("/ganancia-dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<GanaciaDiaDTO> getGananciaDia(@RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, @RequestParam("branchId") Long branchId) {
        BigDecimal ganancia = ventaServiceImpl.obtenerGananciaPorDia(fecha, branchId);
        return ResponseEntity.ok(new GanaciaDiaDTO(fecha, ganancia));
    }


    @PostMapping("/ganancia-rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<BigDecimal> getGananciaPorRango(@RequestBody GananciaPorFechaDTO gananciaPorFechaDTO) {
        return ResponseEntity.ok(ventaServiceImpl.obtenerGananciaPorRango(gananciaPorFechaDTO));
    }
    @PostMapping("/ganancia-diaria-rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<GanaciaDiaDTO>> getGananciaDiariaRango(@RequestBody GananciaPorFechaDTO gananciaPorFechaDTO) {
        Map<LocalDate, BigDecimal> datos = ventaServiceImpl.obtenerGananciasPorDiaEnRango(gananciaPorFechaDTO);

        List<GanaciaDiaDTO> respuesta = datos.entrySet().stream()
                .map(entry -> new GanaciaDiaDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }


    // 1) Ganancia (neta) por venta específica (considerando devoluciones)
    @GetMapping("/ganancia/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<BigDecimal> obtenerGananciaPorVenta(@PathVariable Long ventaId, @RequestParam(required = false) Long branchId) {
        BigDecimal ganancia = ventaServiceImpl.obtenerGananciaPorVenta(ventaId, branchId);
        return ResponseEntity.ok(ganancia);
    }

    // 2) Ventas BRUTAS por rango [desde, hasta]
    @PostMapping("/brutas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<BigDecimal> obtenerVentasBrutasPorRango(
            @RequestBody GananciaPorFechaDTO gananciaPorFechaDTO) {

        BigDecimal total = ventaServiceImpl.obtenerVentasBrutasPorRango(gananciaPorFechaDTO);
        return ResponseEntity.ok(total);
    }

    // 3) Ventas NETAS por rango [desde, hasta] (resta importes devueltos)
    @PostMapping("/netas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<BigDecimal> obtenerVentasNetasPorRango(
            @RequestBody GananciaPorFechaDTO gananciaPorFechaDTO) {

        BigDecimal total = ventaServiceImpl.obtenerVentasNetasPorRango(gananciaPorFechaDTO);
        return ResponseEntity.ok(total);
    }
}
