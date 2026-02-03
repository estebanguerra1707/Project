package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.ProveedorDTO;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;

import java.util.List;

public interface IProveedorService {
    List<ProveedorResponseDTO> getAll();
    ProveedorResponseDTO getById(Long id);
    ProveedorResponseDTO save(ProveedorDTO proveedorDTO);
    ProveedorResponseDTO update(Long id, ProveedorDTO proveedorDTO);
    void disable(Long id);
    List<ProveedorResponseDTO> getByBusinessType(Long businessTypeId);

}
