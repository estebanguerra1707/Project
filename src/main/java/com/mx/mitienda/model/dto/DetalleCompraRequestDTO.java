package com.mx.mitienda.model.dto;

import com.mx.mitienda.model.Compra;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleCompraRequestDTO {
    private Long productId;
    private Integer quantity;
}

