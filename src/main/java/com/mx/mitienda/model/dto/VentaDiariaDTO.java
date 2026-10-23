package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaDiariaDTO {
    private String dia;
    private BigDecimal ingresos;
    private BigDecimal ganancia;
    private BigDecimal chilaquilesVendidos;
    private BigDecimal gastos;

    private BigDecimal costoChilaquiles;
    private BigDecimal costoPanes;
    private BigDecimal costoOtros;

    private BigDecimal piezasChilaquiles;
    private BigDecimal piezasPanes;
    private BigDecimal piezasOtros;
}