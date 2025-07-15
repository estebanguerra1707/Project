package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistorialMovimientosResponseDTO {
    private LocalDateTime movementDate;
    private TipoMovimiento movementType;
    private Integer quantity;
    private Integer beforeStock;
    private Integer newStock;
    private String reference;
}
