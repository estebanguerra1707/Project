package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class DevolucionComprasRequestDTO {
    private Long compraId;
    private String codigoBarras;
    private Integer cantidad;
    private String motivo;
}
