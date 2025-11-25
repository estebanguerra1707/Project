package com.mx.mitienda.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductCategoryResponseDTO {
    private Long id;
    private String name;
    private Long businessTypeId;
    private String businessTypeName;
    private Boolean isActive;
    private LocalDateTime fecha_creacion;
}
