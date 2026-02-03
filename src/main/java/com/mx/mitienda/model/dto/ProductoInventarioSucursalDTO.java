package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class ProductoInventarioSucursalDTO {
    private Long productoId;
    private String productoNombre;
    private Long sucursalId;
    private String sucursalNombre;
    private Integer stock;
}
