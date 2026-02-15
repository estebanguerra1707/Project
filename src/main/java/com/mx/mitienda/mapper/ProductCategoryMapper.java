package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProductCategoryMapper {

    private final BusinessTypeRepository businessTypeRepository;
    private final ProductCategoryRepository productCategoryRepository;

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

        if (productCategoryDTO.getName() == null || productCategoryDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio y no puede estar vacío.");
        }

        if (productCategoryDTO.getBusinessTypeId() == null) {
            throw new IllegalArgumentException("El ID del tipo de negocio es obligatorio.");
        }
        String normalizedName = productCategoryDTO.getName().trim().replaceAll("\\s+", " ");

        BusinessType businessType = businessTypeRepository.findById(productCategoryDTO.getBusinessTypeId())
                .orElseThrow(() -> new NotFoundException("El tipo de negocio no se ha encontrado, intenta con otro."));

        if (productCategoryRepository.existsByBusinessTypeIdAndNameIgnoreCase(productCategoryDTO.getBusinessTypeId(), normalizedName)) {
            throw new IllegalArgumentException("La categoría ya existe intenta crear otra diferente.");
        }

        ProductCategory category = new ProductCategory();
        category.setName(normalizedName);
        category.setBusinessType(businessType);
        category.setActivo(true);
        category.setFecha_creacion(LocalDateTime.now());
        return category;
    }

    public ProductCategory toUpdate(ProductCategory existing, ProductCategoryDTO dto) {

        BusinessType finalBusinessType = existing.getBusinessType();
        if (dto.getBusinessTypeId() != null) {
            finalBusinessType = businessTypeRepository.findById(dto.getBusinessTypeId())
                    .orElseThrow(() -> new NotFoundException("Tipo de negocio no encontrado"));
        }
        String finalName = existing.getName();
        if (dto.getName() != null && !dto.getName().isBlank()) {
            finalName = dto.getName().trim().replaceAll("\\s+", " ");
        }

        if (finalBusinessType != null && finalName != null && !finalName.isBlank()) {
            boolean exists = productCategoryRepository
                    .existsByBusinessTypeIdAndNameIgnoreCaseAndIdNot(
                            finalBusinessType.getId(),
                            finalName,
                            existing.getId()
                    );

            if (exists) {
                throw new IllegalArgumentException("La categoría ya existe. Intenta modificar otra.");
            }
        }
        existing.setBusinessType(finalBusinessType);
        existing.setName(finalName);
        return existing;
    }

}
