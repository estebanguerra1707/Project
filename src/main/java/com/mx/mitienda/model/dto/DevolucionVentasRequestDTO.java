package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class DevolucionVentasRequestDTO {
    private Long ventaId;
    private String codigoBarras;
    private Integer cantidad;
    private String motivo;
    private Long branchId;
    private Long businessTypeId;
}
