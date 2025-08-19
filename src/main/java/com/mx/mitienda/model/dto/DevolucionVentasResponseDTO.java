package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DevolucionVentasResponseDTO {
    private Long id;
    private LocalDateTime fechaDevolucion;
    private String motivo;
    private Long ventaId;
    private String usuario;
    private String sucursal;
    private String tipoDevolucion;
    private BigDecimal totalDevolucion;
    private List<DetalleDevolucionVentaDTO> detalles;

}
