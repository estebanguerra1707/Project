package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DetalleVentaRequestDTO {
    Long productId;
    private BigDecimal quantity;
    private InventarioOwnerType ownerType;

    private List<DetalleVentaExtraRequestDTO> extras;
    private String notes;
}
