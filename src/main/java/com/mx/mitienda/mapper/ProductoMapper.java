package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductoMapper {

    private final BusinessTypeRepository businessTypeRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductDetailMapper productDetailMapper;
    private final ProductoRepository productoRepository;

    public Producto toEntity(ProductoDTO productoDTO) {
        // Cargar la categoría con su businessType
        ProductCategory category = productCategoryRepository.findWithBusinessTypeById(productoDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Proveedor proveedor = proveedorRepository.findById(productoDTO.getProviderId()).orElseThrow(()->new NotFoundException("Proveedor no encontrado"));

        BusinessType businessType = businessTypeRepository.findById(productoDTO.getBusinessTypeId())
                .orElseThrow(() -> new RuntimeException("Tipo de negocio no encontrado"));

        Long categoryBusinessId = category.getBusinessType() != null
                ? category.getBusinessType().getId()
                : null;
        // Validar que la categoría pertenezca al tipo de negocio
        if (!Objects.equals(categoryBusinessId, businessType.getId())){
            throw new IllegalArgumentException("La categoría no pertenece al tipo de negocio proporcionado");
        }

        Producto producto = new Producto();

        producto.setName(productoDTO.getName());
        producto.setSku(productoDTO.getSku());
        producto.setDescription(productoDTO.getDescription());
        producto.setPurchasePrice(productoDTO.getPurchasePrice());
        producto.setActive(true);
        producto.setBusinessType(businessType);
        producto.setProductCategory(category);
        producto.setProvider(proveedor);

        return producto;
    }

    public ProductoResponseDTO toResponse(Producto producto){
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setName(producto.getName());
        response.setSku(producto.getSku());
        response.setDescription(producto.getDescription());
        response.setPurchasePrice(producto.getPurchasePrice());
        response.setBusinessTypeId(producto.getBusinessType().getId());
        response.setBussinessTypeName(producto.getBusinessType().getName());
        response.setCategoryId(producto.getProductCategory().getId());
        response.setCategoryName(producto.getProductCategory().getName());
        response.setProviderId(producto.getProvider().getId());
        response.setProviderName(producto.getProvider().getName());
        if (producto.getProductDetail() != null) {
            response.setProductDetail(productDetailMapper.toResponse(producto.getProductDetail()));
        }
        return response;
    }

    // Actualizar producto existente
    public Producto toUpdate(ProductoDTO productoDTO, Long id) {

        Producto existing = productoRepository.findById(id).orElseThrow(()-> new NotFoundException("Producto no encontrado"));
        if (productoDTO.getSku() != null && !productoDTO.getSku().isBlank()) {
            existing.setSku(productoDTO.getSku());
        }
        if (productoDTO.getDescription() != null && !productoDTO.getDescription().isBlank()) {
            existing.setDescription(productoDTO.getDescription());
        }
        if (productoDTO.getPurchasePrice() != null) {
            existing.setPurchasePrice(productoDTO.getPurchasePrice());
        }
        if (productoDTO.getBusinessTypeId() != null) {
            BusinessType bt = businessTypeRepository.findById(productoDTO.getBusinessTypeId())
                    .orElseThrow(() -> new RuntimeException("BusinessType no encontrado"));
            existing.setBusinessType(bt);
        }
        if (productoDTO.getCategoryId() != null) {
            ProductCategory cat = productCategoryRepository.findById(productoDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            existing.setProductCategory(cat);
        }
        if (productoDTO.getProviderId() != null) {
            Proveedor prov = proveedorRepository.findById(productoDTO.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            existing.setProvider(prov);
        }
        return existing;
    }

    private void validateCreateDTO(ProductoDTO productoDTO) {
        if (productoDTO.getName() == null || productoDTO.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (productoDTO.getSku() == null || productoDTO.getSku().isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio.");
        }
        if (productoDTO.getPurchasePrice() == null) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        if (productoDTO.getBusinessTypeId() == null) {
            throw new IllegalArgumentException("El tipo de negocio es obligatorio.");
        }
        if (productoDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        if (productoDTO.getProviderId() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio.");
        }
    }

}
