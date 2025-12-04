package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ICompraService {
     List<CompraResponseDTO> getAll(String username, String role);
     CompraResponseDTO getById(Long id);
     CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth);
     void inactivePurchase(Long id);
    Page<CompraResponseDTO> advancedSearch(CompraFiltroDTO compraDTO, Pageable pageable);
    List<CompraResponseDTO> findCurrentUserCompras();
    Page<CompraResponseDTO> findByBranchPaginated(Long branchId, int page, int size, String sort);
}
