package com.mx.mitienda.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FiltroDevolucionComprasResponseDTO {
        private Long id;
        private Long compraId;

        private LocalDateTime fechaDevolucion;

        private BigDecimal montoDevuelto;
        private String tipoDevolucion;

        private String productName;
        private String codigoBarras;
        private BigDecimal cantidadDevuelta;

        private String username;
        private String branchName;

}
