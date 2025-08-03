package com.mx.mitienda.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GananciaPorFechaDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
