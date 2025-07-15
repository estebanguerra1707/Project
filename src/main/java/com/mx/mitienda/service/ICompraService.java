package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ICompraService {
    public List<CompraResponseDTO> getAll(String username, String role);
    public CompraResponseDTO getById(Long id);
    public CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth);
    public void inactivePurchase(Long id);
    public List<CompraResponseDTO> advancedSearch(CompraFiltroDTO compraDTO);
    public List<CompraResponseDTO> findCurrentUserCompras();
}
