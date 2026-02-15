package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HistorialMovimientosResponseDTO {
    private LocalDateTime movementDate;
    private TipoMovimiento movementType;
    private BigDecimal quantity;
    private BigDecimal beforeStock;
    private BigDecimal newStock;
    private String reference;
}
