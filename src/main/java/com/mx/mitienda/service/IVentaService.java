package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;

import java.util.List;

public interface IVentaService {
    public VentaResponseDTO registerSell(VentaRequestDTO request, String username);
    public List<VentaResponseDTO> getAll(String username, String rol);
    public List<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, String username, String role);
    public List<DetalleVentaResponseDTO> getDetailsPerSale(Long id);
    public VentaResponseDTO getById(Long id);
    public byte[] generateTicketPdf(Long idVenta);
    public List<VentaResponseDTO> findCurrentUserVentas();
}
