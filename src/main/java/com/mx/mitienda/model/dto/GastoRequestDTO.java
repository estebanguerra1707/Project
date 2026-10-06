package com.mx.mitienda.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GastoRequestDTO {

    private String descripcion;
    private BigDecimal monto;
    private LocalDate fechaGasto;

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFechaGasto() { return fechaGasto; }
    public void setFechaGasto(LocalDate fechaGasto) { this.fechaGasto = fechaGasto; }
}