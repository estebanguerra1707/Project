package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoFiltroDTO {
    private Boolean active;
    private String name;
    private BigDecimal min;
    private BigDecimal max;
    private String category;
    private Boolean availabe;
    private Boolean withoutCategory;
    private Long id;
    private Long categoryId; // <- necesario para relacionar

}
