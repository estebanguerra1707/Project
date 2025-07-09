package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class ProductCategoryResponseDTO {
    private Long id;
    private String name;
    private Long businessTypeId;
    private String businessTypeName;
}
