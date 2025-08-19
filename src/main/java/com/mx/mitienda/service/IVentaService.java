package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IVentaService {
   VentaResponseDTO registerSell(VentaRequestDTO request, String username);
   List<VentaResponseDTO> getAll(String username, String rol);
   List<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, String username, String role);
   List<DetalleVentaResponseDTO> getDetailsPerSale(Long id);
   VentaResponseDTO getById(Long id);
   byte[] generateTicketPdf(Long idVenta);
   List<VentaResponseDTO> findCurrentUserVentas();
   BigDecimal obtenerGananciaHoy();
   BigDecimal obtenerGananciaSemana();
   BigDecimal obtenerGananciaMes();
   BigDecimal obtenerGananciaPorDia(LocalDate dia);
   BigDecimal obtenerGananciaPorRango(LocalDate desde, LocalDate hasta);
   Map<LocalDate, BigDecimal> obtenerGananciasPorDiaEnRango(LocalDate desde, LocalDate hasta);
   BigDecimal obtenerGananciaPorVenta(Long ventaId);
   BigDecimal obtenerVentasBrutasPorRango(LocalDate desde, LocalDate hasta);
   BigDecimal obtenerVentasNetasPorRango(LocalDate desde, LocalDate hasta);
}
