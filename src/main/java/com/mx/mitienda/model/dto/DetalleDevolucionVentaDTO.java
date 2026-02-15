package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleDevolucionVentaDTO {
    private Long productId;
    private String productName;
    private BigDecimal cantidadDevuelta;
    private BigDecimal precioUnitario;
    private InventarioOwnerType inventarioOwnerType;
}
