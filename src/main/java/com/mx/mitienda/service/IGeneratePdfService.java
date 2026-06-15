package com.mx.mitienda.service;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.VentaConsolidadaResponseDTO;


public interface IGeneratePdfService {
    public byte[] generatePdf(String type, Long id, Boolean isPrinted);
    byte[] generateVentaConsolidadaPdf(
            VentaConsolidadaResponseDTO detalle,
            Sucursal branch,
            Boolean isPrinted
    );
}
