package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.SucursalDTO;
import com.mx.mitienda.model.dto.SucursalResponseDTO;

import java.util.List;

public interface ISucursalService {
    SucursalResponseDTO create(SucursalDTO dto);
    SucursalResponseDTO update(Long id, SucursalDTO dto);
    void disable(Long id);
    List<SucursalResponseDTO> findAll();
    SucursalResponseDTO findById(Long id);
}
