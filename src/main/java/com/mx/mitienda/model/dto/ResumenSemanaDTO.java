package com.mx.mitienda.model.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenSemanaDTO {
    private List<VentaDiariaDTO> ventasDiariasSemana;
    private BigDecimal productosVendidosSemana;
}