package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class DetalleVentaRequestDTO {
    Long productId;
    private Integer quantity;
}
