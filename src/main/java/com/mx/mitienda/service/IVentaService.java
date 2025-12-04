package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IVentaService {
    VentaResponseDTO registerSell(VentaRequestDTO request);

    List<VentaResponseDTO> getAll();

    Page<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, int page, int size);

    List<DetalleVentaResponseDTO> getDetailsPerSale(Long id);

    VentaResponseDTO getById(Long id);

    void deleteById(Long id);

    byte[] generateTicketPdf(Long idVenta);

    List<VentaResponseDTO> findCurrentUserVentas();

    BigDecimal obtenerGananciaHoy(Long branchId);

    BigDecimal obtenerGananciaSemana(Long branchId);

    BigDecimal obtenerGananciaMes(Long branchId);

    BigDecimal obtenerGananciaPorDia(LocalDate dia, Long branchId);

    BigDecimal obtenerGananciaPorRango(GananciaPorFechaDTO gananciaPorFechaDTO);

    Map<LocalDate, BigDecimal> obtenerGananciasPorDiaEnRango(GananciaPorFechaDTO gananciaPorFechaDTO);

    BigDecimal obtenerGananciaPorVenta(Long ventaId, Long branchId);

    BigDecimal obtenerVentasBrutasPorRango(GananciaPorFechaDTO gananciaPorFechaDTO);

    BigDecimal obtenerVentasNetasPorRango(GananciaPorFechaDTO gananciaPorFechaDTO);
}