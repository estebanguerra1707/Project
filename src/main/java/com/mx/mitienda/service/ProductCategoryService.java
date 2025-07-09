package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductCategoryMapper;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryResponseDTO save(ProductCategoryDTO dto) {
        ProductCategory category = productCategoryMapper.toEntity(dto);
        return productCategoryMapper.toResponse(categoryRepository.save(category));
    }

    public List<ProductCategoryResponseDTO> getAll() {

        return categoryRepository.findAll().stream().map(productCategoryMapper::toResponse).collect(Collectors.toList());
    }

    public List<ProductCategoryResponseDTO> getByBusinessType(Long businessTypeId) {
        return categoryRepository.findByBusinessTypeId(businessTypeId).stream().map(productCategoryMapper::toResponse).collect(Collectors.toList());
    }

    public ProductCategoryResponseDTO getById(Long id) {
        ProductCategory productCategory =  categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with ID " + id + " not found"));
        return productCategoryMapper.toResponse(productCategory);
    }

    @Transactional
    public ProductCategoryResponseDTO update(Long id, ProductCategoryDTO dto) {
        ProductCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        ProductCategory updated = productCategoryMapper.toUpdate(existing, dto);
        return productCategoryMapper.toResponse(categoryRepository.save(updated));
    }

}
