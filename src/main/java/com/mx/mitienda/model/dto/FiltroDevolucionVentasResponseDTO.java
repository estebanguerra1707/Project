package com.mx.mitienda.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class FiltroDevolucionVentasResponseDTO {
    private Long id;
    private Long ventaId;

    private LocalDateTime fechaDevolucion;

    private BigDecimal montoDevuelto;   // antes totalDevolucion
    private String tipoDevolucion;

    private String username;            // antes usuario
    private String branchName;          // antes sucursal

    private String productName;         // dato directo desde el detalle
    private String productCode;         // código de barras

    private Integer cantidadDevuelta;   // suma o único detalle

    private String motivo;
}
