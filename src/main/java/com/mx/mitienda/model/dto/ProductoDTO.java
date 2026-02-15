package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.UnidadMedida;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductoDTO {
    private String name;
    private String sku;
    private Long categoryId;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Long providerId;
    private LocalDateTime updatedDate;
    private String codigoBarras;
    private Long unidadMedidaId;
    private Long branchId;
}
