package com.mx.mitienda.service;

import com.mx.mitienda.context.ScopeResolver;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.specification.ProductoSpecification;
import com.mx.mitienda.util.ProductoSpecBuilder;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductoServiceImpl extends BaseService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final UsuarioRepository usuarioRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private static final Map<String, String> SORT_ALIAS = Map.of(
            "product", "name",               // Si el front manda 'product', usa 'name'
            "category", "productCategory.name" // Ejemplo extra opcional
    );

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "id", "name", "sku", "purchasePrice", "salePrice",
            "active", "creationDate", "codigoBarras", "productCategory.name"
    );

    public ProductoServiceImpl(
            IAuthenticatedUserService authenticatedUserService,
            ProductoRepository productoRepository,
            ProductoMapper productoMapper,
            UsuarioRepository usuarioRepository,
            ProductCategoryRepository productCategoryRepository
    ) {
        super(authenticatedUserService); // 👈 pasa la dependencia al BaseService
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.usuarioRepository = usuarioRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    private Pageable sanitize(Pageable pageable) {
        Sort sanitized = Sort.unsorted();

        for (Sort.Order o : pageable.getSort()) {
            String requested = o.getProperty();
            String mapped = SORT_ALIAS.getOrDefault(requested, requested);

            if (ALLOWED_SORTS.contains(mapped)) {
                sanitized = sanitized.and(Sort.by(new Sort.Order(o.getDirection(), mapped)));
            }

        }
        if (sanitized.isUnsorted()) {
            sanitized = Sort.by(Sort.Order.asc("name"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sanitized);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> getAll(ProductoFiltroDTO filtro, Pageable pageable) {
        UserContext ctx = ctx();

        log.info("BUSINESS_TYPE_ID={} | BRANCH_ID={} | isSuper={}",
                ctx.getBusinessTypeId(), ctx.getBranchId(), ctx.isSuperAdmin());

        var spec = ProductoSpecification.byBusinessTypeAndBranch(
                ctx.getBusinessTypeId(), ctx.getBranchId(), ctx.isSuperAdmin());

        Pageable safePageable = sanitize(pageable);

        Page<Producto> page = productoRepository.findAll(spec, safePageable);
        return page.map(productoMapper::toResponse);
    }

    public ProductoResponseDTO getById(Long idProducto){
        UserContext ctx = ctx();
        final Producto producto;
        if (ctx.isSuperAdmin()) {
            producto = productoRepository.findById(idProducto)
                    .filter(Producto::getActive)
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        } else {
            Long branchId = ctx.getBranchId();
            producto = productoRepository
                    .findActiveWithDetailByIdAndSucursal(idProducto, branchId)
                    .orElseThrow(() -> new NotFoundException(
                            "Producto no encontrado en la sucursal del usuario logueado"));
        }
        return productoMapper.toResponse(producto);
    }

    public ProductoResponseDTO save(ProductoDTO productoDTO){

        // Obtener la categoría para acceder al tipo de negocio
        ProductCategory productCategory = productCategoryRepository.findById(productoDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

        Long businessTypeId = productCategory.getBusinessType().getId();

        // Validaciones únicas por tipo de negocio
        if (productoRepository.existsByCodigoBarrasAndProductCategory_BusinessType_Id(productoDTO.getCodigoBarras(), businessTypeId)) {
            throw new IllegalArgumentException("Ya existe un producto con este código de barras en este tipo de negocio.");
        }
        if (productoRepository.existsBySkuAndProductCategory_BusinessType_Id(productoDTO.getSku(), businessTypeId)) {
            throw new IllegalArgumentException("Ya existe un producto con este SKU en este tipo de negocio.");
        }
        if (productoRepository.existsByNameIgnoreCaseAndProductCategory_BusinessType_Id(productoDTO.getName(), businessTypeId)) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre en este tipo de negocio.");
        }

        Producto entity = productoMapper.toEntity(productoDTO);
        Producto saved = productoRepository.save(entity);
        return productoMapper.toResponse(saved);

    }

    public void disableProduct(Long id){
        Producto producto = productoRepository.findById(id).orElseThrow(()-> new NotFoundException("Producto no encontrado"));
        producto.setActive(false);
        productoRepository.save(producto);
    }

    @Transactional
    public ProductoResponseDTO updateProduct(ProductoDTO updatedProduct, Long id){
       Producto existProduct = productoMapper.toUpdate(updatedProduct, id);
       Producto producto = productoRepository.save(existProduct);
        return productoMapper.toResponse(producto);
    }

    public Page<ProductoResponseDTO> buscarAvanzado(ProductoFiltroDTO productDTO, Pageable pageable) {
        var ctx = ctx();

        if (!ctx.isSuperAdmin()) {
            productDTO.setBranchId(ctx.getBranchId());
        }

        Specification<Producto> spec = ProductoSpecBuilder.fromDTO(productDTO);
        Page<Producto> page = productoRepository.findAll(spec, pageable);
        return page.map(productoMapper::toResponse);
    }


    @Override
    public ProductoResponseDTO buscarPorCodigoBarras(String codigoBarras) {
        var ctx = ctx();
        Long businessType = ctx.getBusinessTypeId();
        Producto producto = productoRepository.findByCodigoBarrasAndBusinessTypeId(codigoBarras, businessType)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ese código de barras"));
        return productoMapper.toResponse(producto);
    }

    private boolean isSuperAdmin(Usuario u) {
        return u != null && u.getRole() == Rol.SUPER_ADMIN; // ajusta si tu Rol es String
    }

}
