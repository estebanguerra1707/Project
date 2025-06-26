package com.mx.mitienda.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class VentaRequestDTO {
    private Long clientId;
    private List<DetalleVentaRequest> detatails;

}
