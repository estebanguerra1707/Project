package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.BusinessTypeDTO;
import com.mx.mitienda.model.dto.BusinessTypeResponseDTO;

import java.util.List;

public interface IBusinessTypeService {
    BusinessTypeResponseDTO save(BusinessTypeDTO dto);
    List<BusinessTypeResponseDTO> getAllOrderById();
    public void delete(Long id);
    public BusinessTypeResponseDTO update(Long id, BusinessTypeDTO dto);
    public BusinessTypeResponseDTO getById(Long id);
}
