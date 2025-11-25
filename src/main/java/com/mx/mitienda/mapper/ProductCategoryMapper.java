package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProductCategoryMapper {

    private final BusinessTypeRepository businessTypeRepository;

    public ProductCategoryResponseDTO toResponse(ProductCategory category) {
        ProductCategoryResponseDTO dto = new ProductCategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        if (category.getBusinessType() != null) {
            dto.setBusinessTypeId(category.getBusinessType().getId());
            dto.setBusinessTypeName(category.getBusinessType().getName());
        }
        dto.setIsActive(category.getActivo());
        dto.setFecha_creacion(category.getFecha_creacion());
        return dto;
    }
    public ProductCategory toEntity(ProductCategoryDTO productCategoryDTO) {

        if (productCategoryDTO.getName() == null || productCategoryDTO.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio y no puede estar vacío.");
        }

        if (productCategoryDTO.getBusinessTypeId() == null) {
            throw new IllegalArgumentException("El ID del tipo de negocio es obligatorio.");
        }
        BusinessType businessType = businessTypeRepository.findById(productCategoryDTO.getBusinessTypeId())
                .orElseThrow(() -> new NotFoundException(
                        "El tipo de negocio no se ha encontrado, intenta con otro."));
        ProductCategory category = new ProductCategory();
        category.setName(productCategoryDTO.getName());
        category.setBusinessType(businessType);
        category.setActivo(true);
        category.setFecha_creacion(LocalDateTime.now());
        return category;

    }

    public ProductCategory toUpdate(ProductCategory existing, ProductCategoryDTO dto) {
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        // Si mandan un businessTypeId válido, actualiza
        if (dto.getBusinessTypeId() != null) {
            BusinessType businessType = businessTypeRepository.findById(dto.getBusinessTypeId())
                    .orElseThrow(() -> new NotFoundException("Tipo de negocio no encontrado"));
            existing.setBusinessType(businessType);
        }
        existing.setFecha_creacion(LocalDateTime.now());
        return existing;
    }
}
