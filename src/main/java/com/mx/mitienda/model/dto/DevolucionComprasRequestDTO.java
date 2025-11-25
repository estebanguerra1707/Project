package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class DevolucionComprasRequestDTO {
    private Long compraId;
    private Long branchId;
    private Long businessTypeId;
    private String codigoBarras;
    private Integer cantidad;
    private String motivo;
}
