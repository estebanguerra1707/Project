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
    private Boolean available;
    private Boolean withoutCategory;
    private Long id;
    private Long categoryId;
    private Long branchId;
    private Long businessTypeId;
    private String codigoBarras;

}
