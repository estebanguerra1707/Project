package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryFiltroDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductCategoryService {
     ProductCategoryResponseDTO save(ProductCategoryDTO dto);
    Page<ProductCategoryResponseDTO> getAll(ProductCategoryFiltroDTO filtro, Pageable pageable);
     List<ProductCategoryResponseDTO> getByBusinessType(Long businessTypeId);
     ProductCategoryResponseDTO getById(Long id);
     ProductCategoryResponseDTO update(Long id, ProductCategoryDTO dto);
     List<ProductCategoryResponseDTO> getByCurrentUserBusinessType();
     List<ProductCategoryResponseDTO> getActualCatalog();
    List<ProductCategoryResponseDTO> findByBusinessTypeAsList(Long businessTypeId);
    void disableCategory(Long id);
}
