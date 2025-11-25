package com.mx.mitienda.model.dto;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DevolucionesVentasFiltroDTO {

    // ID de la devolución
    private Long id;

    // ID de la venta a la que pertenece
    private Long ventaId;

    // Usuario que hizo la devolución (username o email, como prefieras)
    private String username;

    // Solo SUPER_ADMIN puede filtrar por sucursal
    private Long branchId;

    // Rango de fechas
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    // Por día / mes / año de la fecha de devolución
    private Integer day;
    private Integer month;
    private Integer year;

    // Monto devuelto (min / max)
    private BigDecimal minMonto;
    private BigDecimal maxMonto;

    // Cantidad devuelta por detalle (min / max)
    private Integer minCantidad;
    private Integer maxCantidad;

    // Tipo de devolución: TOTAL o PARCIAL
    private String tipoDevolucion;

    // Producto
    private String codigoBarras;
    private String productName;
}
