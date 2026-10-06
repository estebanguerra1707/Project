package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardResumenDTO {
    private long totalProductos;
    private long productosCriticos;
    private long ventasHoy;
    private BigDecimal ingresosMes;

    private BigDecimal productosVendidosHoy;
    private BigDecimal productosVendidosSemana;
    private BigDecimal productosVendidosMes;

    private List<UsuarioVentaResumenDTO> ventasHoyPorUsuario;
    private List<UsuarioVentaResumenDTO> ingresosMesPorUsuario;
    private BigDecimal ingresosHoy;
    private BigDecimal gananciaHoy;
    private List<VentaDiariaDTO> ventasDiariasSemana;
}