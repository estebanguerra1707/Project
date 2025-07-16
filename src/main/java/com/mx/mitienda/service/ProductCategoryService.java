package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductCategoryMapper;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductCategoryRepository;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCategoryService implements IProductCategoryService{

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final IAuthenticatedUserService authenticatedUserService;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final BusinessTypeRepository businessTypeRepository;

    @Override
    public ProductCategoryResponseDTO save(ProductCategoryDTO dto) {
        ProductCategory category = productCategoryMapper.toEntity(dto);
        return productCategoryMapper.toResponse(categoryRepository.save(category));
    }
    @Override
    public List<ProductCategoryResponseDTO> getAll() {

        return categoryRepository.findAll().stream().map(productCategoryMapper::toResponse).collect(Collectors.toList());
    }
    @Override
    public List<ProductCategoryResponseDTO> getByBusinessType(Long businessTypeId) {
        return categoryRepository.findByBusinessTypeId(businessTypeId).stream().map(productCategoryMapper::toResponse).collect(Collectors.toList());
    }
    @Override
    public ProductCategoryResponseDTO getById(Long id) {
        ProductCategory productCategory =  categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with ID " + id + " not found"));
        return productCategoryMapper.toResponse(productCategory);
    }

    @Transactional
    @Override
    public ProductCategoryResponseDTO update(Long id, ProductCategoryDTO dto) {
        ProductCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        ProductCategory updated = productCategoryMapper.toUpdate(existing, dto);
        return productCategoryMapper.toResponse(categoryRepository.save(updated));
    }

    @Override
    public List<ProductCategoryResponseDTO> getByCurrentUserBusinessType() {
        Long businessTypeId = authenticatedUserService.getBusinessTypeIdFromSession();

        BusinessType businessType = businessTypeRepository.findByIdAndActiveTrue(businessTypeId)
                .orElseThrow(() -> new NotFoundException("Tipo de negocio no encontrado"));

        List<ProductCategory> categorias = categoryRepository.findByBusinessTypeId(businessTypeId);

        if (categorias.isEmpty()) {
            throw new NotFoundException("No hay categorías activas para el tipo de negocio: " + businessType.getName());
        }

        return categorias.stream()
                .map(productCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

}
