package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductCategoryMapper;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.ProductCategoryDTO;
import com.mx.mitienda.model.dto.ProductCategoryFiltroDTO;
import com.mx.mitienda.model.dto.ProductCategoryResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductCategoryRepository;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.specification.ProductCategorySpecification;
import com.mx.mitienda.util.PageUtils;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.PageUtils.sanitize;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductCategoryService implements IProductCategoryService{

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final IAuthenticatedUserService authenticatedUserService;
    private final BusinessTypeRepository businessTypeRepository;
    @Override
    public ProductCategoryResponseDTO save(ProductCategoryDTO dto) {
        ProductCategory category = productCategoryMapper.toEntity(dto);
        return productCategoryMapper.toResponse(categoryRepository.save(category));
    }


    @Transactional(readOnly = true)
    @Override
    public Page<ProductCategoryResponseDTO> getAll(ProductCategoryFiltroDTO filtro, Pageable pageable) {
        boolean isSuper = authenticatedUserService.isSuperAdmin();
        Long enforcedBusinessTypeId = null;

        if (!isSuper) {
            Long currentBt = authenticatedUserService.getCurrentBusinessTypeId();
            if (currentBt == null) {
                throw new ForbiddenException("Tu sucursal no tiene tipo de negocio asignado.");
            }
            enforcedBusinessTypeId = currentBt;
        }
        log.info("CATEGORIAS :: enforcedBT={} filtroBT={} isSuper={}",
                enforcedBusinessTypeId, filtro != null ? filtro.getBusinessTypeId() : null, isSuper);
        Pageable safePageable = sanitize(pageable);

        Specification<ProductCategory> spec =
                ProductCategorySpecification.byFilters(enforcedBusinessTypeId, filtro);

        Page<ProductCategory> page = categoryRepository.findAll(spec, safePageable);
        return page.map(productCategoryMapper::toResponse);
    }



    @Transactional(readOnly = true)
    @Override
    public List<ProductCategoryResponseDTO> getActualCatalog() {
        // Para el <select> del front: categorías según BT del usuario (sin paginar)
        Long btId = authenticatedUserService.getCurrentBusinessTypeId();
        if (btId == null) {
            throw new ForbiddenException("Tu sucursal no tiene tipo de negocio asignado.");
        }
        return categoryRepository.findByBusinessTypeId(btId).stream()
                .map(productCategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductCategoryResponseDTO> findByBusinessTypeAsList(Long businessTypeId) {
        return categoryRepository.findByBusinessTypeId(businessTypeId).stream().map(productCategoryMapper::toResponse).toList();
    }

    @Override
    public List<ProductCategoryResponseDTO> getByBusinessType(Long businessTypeId) {
        return categoryRepository.findByBusinessTypeId(businessTypeId).stream().map(productCategoryMapper::toResponse).collect(Collectors.toList());
    }
    @Override
    public ProductCategoryResponseDTO getById(Long id) {
        Long businessTypeId = authenticatedUserService.getCurrentBusinessTypeId();

        if (authenticatedUserService.isSuperAdmin()) {
            ProductCategory productCategory =  categoryRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Categoria con ID " + id + " no encontrada"));
            return productCategoryMapper.toResponse(productCategory);
        }

        ProductCategory productCategory = categoryRepository.findByIdAndBusinessTypeId(id, businessTypeId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada para tu tipo de negocio."));

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
