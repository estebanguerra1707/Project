package com.mx.mitienda.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)//Solo muestra campos NO nulos
public class ProductoResponseDTO {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Long categoryId;
    private String categoryName;
    private Long providerId;
    private String providerName;
    private Long businessTypeId;
    private String businessTypeName;
    private LocalDateTime creationDate;
    private ProductoDetailResponseDTO productDetail;
    private String codigoBarras;
    private Long branchId;
    private String branchName;
    private Integer stock;
    private Boolean active;
    private InventarioOwnerType inventarioOwnerType;
    private Boolean usaInventarioPorDuenio;
}
