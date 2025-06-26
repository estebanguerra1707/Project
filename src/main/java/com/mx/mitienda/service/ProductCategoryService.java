package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final BusinessTypeRepository businessTypeRepository;

    public ProductCategoryService(ProductCategoryRepository categoryRepository, BusinessTypeRepository businessTypeRepository) {
        this.categoryRepository = categoryRepository;
        this.businessTypeRepository = businessTypeRepository;
    }

    public ProductCategory save(ProductCategoryDTO dto) {
        BusinessType businessType = businessTypeRepository.findById(dto.getBusinessTypeId())
                .orElseThrow(() -> new NotFoundException("BusinessType with ID " + dto.getBusinessTypeId() + " not found"));

        ProductCategory category = new ProductCategory();
        category.setName(dto.getName());
        category.setBusinessType(businessType);

        return categoryRepository.save(category);
    }

    public List<ProductCategory> getAll() {
        return categoryRepository.findAll();
    }

    public List<ProductCategory> getByBusinessType(Long businessTypeId) {
        return categoryRepository.findByBusinessTypeId(businessTypeId);
    }

    public ProductCategory getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with ID " + id + " not found"));
    }

}
