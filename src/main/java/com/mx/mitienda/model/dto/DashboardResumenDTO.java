package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardResumenDTO {
    private long totalProductos;
    private long productosCriticos;
    private long ventasHoy;
    private BigDecimal ingresosMes;
}