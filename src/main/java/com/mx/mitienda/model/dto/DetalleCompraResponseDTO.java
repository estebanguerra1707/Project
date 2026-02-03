package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleCompraResponseDTO {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long businessTypeId;
    private String businessTypeName;
    private String codigoBarras;
    private String sku;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private InventarioOwnerType inventarioOwnerType;
    private Boolean usaInventarioPorDuenio;
}
