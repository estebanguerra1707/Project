package com.mx.mitienda.mapper;

import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.dto.BusinessTypeDTO;
import com.mx.mitienda.model.dto.BusinessTypeResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class BusinessTypeMapper {

    public BusinessType toEntity(BusinessTypeDTO dto) {
        BusinessType type = new BusinessType();
        type.setCode(dto.getCode());
        type.setName(dto.getName());
        return type;
    }

    public BusinessTypeResponseDTO toResponse(BusinessType type) {
        BusinessTypeResponseDTO dto = new BusinessTypeResponseDTO();
        dto.setId(type.getId());
        dto.setName(type.getName());
        dto.setCode(type.getCode());
        return dto;
    }
    // 👇 Método para aplicar actualización parcial
    public void updateEntity(BusinessType existing, BusinessTypeDTO businessTypeDTO) {
        if (businessTypeDTO.getName() != null && !businessTypeDTO.getName().isBlank()) {
            existing.setName(businessTypeDTO.getName());
        }
        if(businessTypeDTO.getCode()!= null && !businessTypeDTO.getCode().isBlank()){
            existing.setCode(businessTypeDTO.getCode());
        }
    }
}