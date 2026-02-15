package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DevolucionComprasRequestDTO {
    private Long compraId;
    private Long branchId;
    private Long businessTypeId;
    private String codigoBarras;
    private BigDecimal cantidad;
    private String motivo;
}
