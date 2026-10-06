package com.mx.mitienda.model.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleVentaExtraResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal extraPrice;
}