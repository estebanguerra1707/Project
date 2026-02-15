package com.mx.mitienda.mapper;

import com.mx.mitienda.context.Scope;
import com.mx.mitienda.context.ScopeResolver;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.IAuthenticatedUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductoMapper {

    private final BusinessTypeRepository businessTypeRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductDetailMapper productDetailMapper;
    private final ProductoRepository productoRepository;
    private final ScopeResolver scopeResolver;
    private final SucursalRepository sucursalRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;


    @Transactional
    public Producto toEntity(ProductoDTO productoDTO) {
        validateCreateDTO(productoDTO);


        Scope scope = scopeResolver.resolveForProductCreate(productoDTO);
        Long businessTypeId = scope.businessTypeId();
        Sucursal sucursal = scope.sucursal();

        // Cargar la categoría con su businessType
        ProductCategory category = productCategoryRepository.findWithBusinessTypeById(productoDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Proveedor proveedor = proveedorRepository.findById(productoDTO.getProviderId()).orElseThrow(()->new NotFoundException("Proveedor no encontrado"));

        BusinessType businessType = businessTypeRepository.findById(businessTypeId)
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
        producto.setSalePrice(productoDTO.getSalePrice());
        producto.setActive(true);
        producto.setProductCategory(category);
        producto.setProvider(proveedor);
        producto.setCreationDate(LocalDateTime.now());
        producto.setUpdatedAt(LocalDateTime.now());
        producto.setBranch(sucursal);
        producto.setBusinessType(businessType);
        UnidadMedidaEntity um = unidadMedidaRepository.findByIdAndActiveTrue(productoDTO.getUnidadMedidaId())
                .orElseThrow(() -> new NotFoundException("Unidad de medida no encontrada"));
        producto.setUnidadMedida(um);

// opcional: sincroniza permiteDecimales automáticamente
        producto.setPermiteDecimales(um.isPermiteDecimales());
        producto.setCodigoBarras(productoDTO.getCodigoBarras());
        return producto;
    }

    public ProductoResponseDTO toResponse(Producto producto){
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setName(producto.getName());
        response.setSku(producto.getSku());
        response.setCodigoBarras(producto.getCodigoBarras());
        response.setDescription(producto.getDescription());
        response.setPurchasePrice(producto.getPurchasePrice());
        response.setSalePrice(producto.getSalePrice());
        response.setCategoryId(producto.getProductCategory().getId());
        response.setCategoryName(producto.getProductCategory().getName());
        response.setProviderId(producto.getProvider().getId());
        response.setProviderName(producto.getProvider().getName());
        response.setBusinessTypeId(
                producto.getProductCategory() != null && producto.getProductCategory().getBusinessType() != null
                        ? producto.getProductCategory().getBusinessType().getId()
                        : null
        );
        response.setBusinessTypeName(
                producto.getProductCategory() != null && producto.getProductCategory().getBusinessType() != null
                        ? producto.getProductCategory().getBusinessType().getName()
                        : null
        );
        if (producto.getProductDetail() != null) {
            response.setProductDetail(productDetailMapper.toResponse(producto.getProductDetail()));
        }
        response.setCreationDate(producto.getCreationDate());
        response.setUpdatedAt(producto.getUpdatedAt());
        response.setCodigoBarras(producto.getCodigoBarras());

        var branch = producto.getBranch();
        if (branch != null) {
            response.setBranchId(branch.getId());
            response.setBranchName(branch.getName());
        } else {
            response.setBranchId(null);
            response.setBranchName(null);
        }
        response.setActive(producto.getActive());
        var um = producto.getUnidadMedida();
        if (um != null) {
            response.setUnidadMedidaId(um.getId());
            response.setUnidadMedidaAbreviatura(um.getAbreviatura());
            response.setUnidadMedidaCodigo(um.getCodigo());
            response.setUnidadMedidaNombre(um.getNombre());
            response.setPermiteDecimales(producto.isPermiteDecimales());
        }
        return response;
    }

    // Actualizar producto existente
    public Producto toUpdate(ProductoDTO dto, Producto existing) {
            if (dto.getName() != null && !dto.getName().isBlank()) {
                existing.setName(dto.getName());
            }

            if (dto.getSku() != null && !dto.getSku().isBlank()) {
                existing.setSku(dto.getSku());
            }

            if (dto.getCodigoBarras() != null && !dto.getCodigoBarras().isBlank()) {
                existing.setCodigoBarras(dto.getCodigoBarras());
            }

            if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
                existing.setDescription(dto.getDescription());
            }

            if (dto.getPurchasePrice() != null) {
                existing.setPurchasePrice(dto.getPurchasePrice());
            }

            if (dto.getSalePrice() != null) {
                existing.setSalePrice(dto.getSalePrice());
            }

            if (dto.getCategoryId() != null) {
                ProductCategory cat = productCategoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
                existing.setProductCategory(cat);

                if (cat.getBusinessType() != null) {
                    existing.setBusinessType(cat.getBusinessType());
                }
            }

            if (dto.getProviderId() != null) {
                Proveedor prov = proveedorRepository.findById(dto.getProviderId())
                        .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
                existing.setProvider(prov);
            }

            if (dto.getBranchId() != null) {
                Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(dto.getBranchId())
                        .orElseThrow(() -> new RuntimeException("La sucursal no ha sido encontrada"));
                existing.setBranch(sucursal);
            }

            if (dto.getUnidadMedidaId() != null) {
                UnidadMedidaEntity um = unidadMedidaRepository.findByIdAndActiveTrue(dto.getUnidadMedidaId())
                        .orElseThrow(() -> new NotFoundException("Unidad de medida no encontrada"));

                existing.setUnidadMedida(um);
                existing.setPermiteDecimales(um.isPermiteDecimales());
            }

            existing.setUpdatedAt(LocalDateTime.now());
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
        if (productoDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        if (productoDTO.getProviderId() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio.");
        }
        if (productoDTO.getCodigoBarras() == null || productoDTO.getCodigoBarras().isBlank()) {
            throw new IllegalArgumentException("El código de barras es obligatorio.");
        }
        if (productoDTO.getUnidadMedidaId() == null) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        }
    }

}
