package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class ProductoDetailResponseDTO {
    private Long id;
    private String partNumber;
    private String carBrand;
    private String carModel;
    private String yearRange;
    private String oemEquivalent;
    private String technicalDescription;
}
