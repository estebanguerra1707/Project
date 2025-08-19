package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.DevolucionComprasReponseDTO;
import com.mx.mitienda.model.dto.DevolucionComprasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public interface IDevolucionComprasService {
    DevolucionComprasReponseDTO procesarDevolucion(DevolucionComprasRequestDTO devolucionVentasRequestDTO, Authentication auth) ;
    // Usa la sucursal del usuario logueado
    Map<LocalDate, Long> contarPorDia(LocalDate desde, LocalDate hasta, Long branchId);
    Map<LocalDate, Long> contarPorSemana(LocalDate desde, LocalDate hasta, Long branchId); // clave = Lunes de la semana
    Map<YearMonth, Long> contarPorMes(LocalDate desde, LocalDate hasta, Long branchId);    // clave = YearMonth del mes
    BigDecimal obtenerDevolucionesComprasPorRango(LocalDate desde, LocalDate hasta);
}
