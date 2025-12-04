package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.DashboardResumenDTO;
import com.mx.mitienda.model.dto.TopProductoDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IDashboardService {
    List<TopProductoDTO> getTopProductos(String groupBy, LocalDateTime start, LocalDateTime end, Long branchId);
    DashboardResumenDTO obtenerResumen(Long branchId);
    List<TopProductoDTO> topVendidos(LocalDate inicio, LocalDate fin, Long branchId);
    List<TopProductoDTO> topVendidosPorUsuario(LocalDate inicio, LocalDate fin, Long branchId);
}
