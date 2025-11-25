package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DevolucionComprasFiltroDTO {
    private Long devolucionId;
    private Long compraId;
    private String codigoBarras;
    private String username;
    private String tipoDevolucion;

    private LocalDateTime start;
    private LocalDateTime end;

    private Integer day;
    private Integer month;
    private Integer year;

    private BigDecimal minMonto;
    private BigDecimal maxMonto;

    private Integer minCantidad;
    private Integer maxCantidad;

    private Long branchId;
}
