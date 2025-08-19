package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ICompraService {
     List<CompraResponseDTO> getAll(String username, String role);
     CompraResponseDTO getById(Long id);
     CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth);
     void inactivePurchase(Long id);
     List<CompraResponseDTO> advancedSearch(CompraFiltroDTO compraDTO);
     List<CompraResponseDTO> findCurrentUserCompras();
}
