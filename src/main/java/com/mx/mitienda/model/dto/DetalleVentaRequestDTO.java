package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

@Data
public class DetalleVentaRequestDTO {
    Long productId;
    private Integer quantity;
    private InventarioOwnerType ownerType;
}
