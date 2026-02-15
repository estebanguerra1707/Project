package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DetalleVentaResponseDTO {
    private Long productId;
    private String productName;
    private String sku;
    private Long branchId;
    private String branchName;
    private Long businessTypeId;
    private String businessTypeName;
    private String codigoBarras;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private Long unitId;
    private String unitAbbr;
    private String unitName;
    private Boolean permiteDecimales;
    private InventarioOwnerType inventarioOwnerType;
    private Boolean usaInventarioPorDuenio;
}
