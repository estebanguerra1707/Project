package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DevolucionVentasRequestDTO {
    private Long ventaId;
    private String codigoBarras;
    private BigDecimal cantidad;
    private String motivo;
    private Long branchId;
    private Long businessTypeId;
}
