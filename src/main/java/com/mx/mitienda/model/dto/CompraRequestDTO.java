package com.mx.mitienda.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraRequestDTO {
    private Long providerId;
    private LocalDateTime purchaseDate;
    private List<DetalleCompraRequestDTO> details;
}
