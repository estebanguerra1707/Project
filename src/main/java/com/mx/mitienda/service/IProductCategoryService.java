package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;

import java.util.List;

public interface IProductCategoryService {
    public ProductCategoryResponseDTO save(ProductCategoryDTO dto);
    public List<ProductCategoryResponseDTO> getAll();
    public List<ProductCategoryResponseDTO> getByBusinessType(Long businessTypeId);
    public ProductCategoryResponseDTO getById(Long id);
    public ProductCategoryResponseDTO update(Long id, ProductCategoryDTO dto);
    public List<ProductCategoryResponseDTO> getByCurrentUserBusinessType();
}
