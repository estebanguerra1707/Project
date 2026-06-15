package com.mx.mitienda.model.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaConsolidadaRequestDTO {

    private Long clienteId;

    private Long userId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime endDate;

    @NotEmpty(message = "Debe seleccionar al menos una venta")
    private List<Long> ventaIds;
}