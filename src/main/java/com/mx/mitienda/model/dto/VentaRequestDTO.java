package com.mx.mitienda.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaRequestDTO {
    Long customerId;
    LocalDateTime saleDate;
    List<DetalleVentaRequestDTO> details;
}
