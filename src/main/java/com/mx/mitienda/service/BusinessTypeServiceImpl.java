package com.mx.mitienda.service;


import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.BusinessTypeMapper;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.dto.BusinessTypeDTO;
import com.mx.mitienda.model.dto.BusinessTypeResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessTypeServiceImpl implements IBusinessTypeService {

    private final BusinessTypeRepository businessTypeRepository;
    private final BusinessTypeMapper businessTypeMapper;

    @Override
    public BusinessTypeResponseDTO save(BusinessTypeDTO businessTypeDTO) {
        BusinessType type = businessTypeMapper.toEntity(businessTypeDTO);
        return businessTypeMapper.toResponse(businessTypeRepository.save(type));
    }

    @Override
    public List<BusinessTypeResponseDTO> getAllOrderById() {
        return businessTypeRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(businessTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BusinessTypeResponseDTO getById(Long id) {
        BusinessType businessType = businessTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("El negocio que buscas no existe, intenta con otro"));
        return businessTypeMapper.toResponse(businessType);
    }

    @Transactional
    public void delete(Long id) {
        BusinessType businessType = businessTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("El negocio que buscas no existe, intenta con otro"));
        businessType.setActive(false);
        businessTypeRepository.save(businessType);
    }

    @Transactional
    public BusinessTypeResponseDTO update(Long id, BusinessTypeDTO businessTypeDTO) {
        BusinessType businessType = businessTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("El negocio que buscas no existe, intenta con otro"));
        businessTypeMapper.updateEntity(businessType, businessTypeDTO);
        return businessTypeMapper.toResponse(businessTypeRepository.save(businessType));
    }
}