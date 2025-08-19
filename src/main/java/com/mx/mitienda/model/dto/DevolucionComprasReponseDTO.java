package com.mx.mitienda.model.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DevolucionComprasReponseDTO {
    private Long id;
    private LocalDateTime fechaDevolucion;
    private String motivo;
    private Long compraId;
    private String usuario;
    private String sucursal;
    private String tipoDevolucion;
    private BigDecimal totalDevolucion;
    private List<DetalleDevolucionCompraDTO> detalles;
}
