package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.util.enums.UnidadMedida;
import lombok.Data;
import lombok.Locked;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventarioSucursalResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long branchId;
    private String branchName;
    private String sku;
    private BigDecimal stock;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private LocalDateTime lastUpdated;
    private String updatedBy;
    private Long unitId;
    private String unitAbbr;
    private String unitName;
    private Boolean permiteDecimales;
    private UnidadMedida unidadMedida;
    private Boolean isStockCritico;
    private InventarioOwnerType ownerType;

}