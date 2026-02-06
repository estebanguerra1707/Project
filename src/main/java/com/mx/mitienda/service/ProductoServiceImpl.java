package com.mx.mitienda.service;

import com.mx.mitienda.context.ScopeResolver;
import com.mx.mitienda.exception.BadRequestException;
import com.mx.mitienda.exception.ForbiddenException;
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
import com.mx.mitienda.util.enums.InventarioOwnerType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductoServiceImpl extends BaseService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ProductCategoryRepository productCategoryRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;

    private static final Map<String, String> SORT_ALIAS = Map.of(
            "product", "name",               // Si el front manda 'product', usa 'name'
            "category", "productCategory.name" // Ejemplo extra opcional
    );

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "id", "name", "sku", "purchasePrice", "salePrice",
            "active", "creationDate", "updatedAt", "codigoBarras", "productCategory.name"
    );

    public ProductoServiceImpl(
            IAuthenticatedUserService authenticatedUserService,
            ProductoRepository productoRepository,
            ProductoMapper productoMapper,
            UsuarioRepository usuarioRepository,
            ProductCategoryRepository productCategoryRepository,
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository inventarioSucursalRepository
    ) {
        super(authenticatedUserService);
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.productCategoryRepository = productCategoryRepository;
        this.sucursalRepository = sucursalRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
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
        Long effectiveBranchId;
        if (ctx.isSuperAdmin()) {
            effectiveBranchId = filtro.getBranchId();
        } else {
            effectiveBranchId = ctx.getBranchId();
        }
        var spec = ProductoSpecification.byBusinessTypeAndBranch(
                ctx.getBusinessTypeId(),
                effectiveBranchId,
                ctx.isSuperAdmin()
        );
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

    @Transactional
    public ProductoResponseDTO save(ProductoDTO productoDTO) {
        UserContext ctx = ctx();

        if (!(ctx.isAdmin() || ctx.isSuperAdmin())) {
            throw new ForbiddenException("No tienes permisos para crear productos");
        }

        Long branchIdEfectivo = ctx.isSuperAdmin()
                ? productoDTO.getBranchId()
                : ctx.getBranchId();

        if (branchIdEfectivo == null) {
            throw new BadRequestException("No se pudo determinar la sucursal para el inventario inicial del producto");
        }

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchIdEfectivo)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la sucursal"));

        ProductCategory productCategory = productCategoryRepository.findById(productoDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

        Long businessTypeId = productCategory.getBusinessType().getId();

        String sku = productoDTO.getSku();
        String barcode = productoDTO.getCodigoBarras();

        boolean hasSku = sku != null && !sku.isBlank();
        boolean hasBarcode = barcode != null && !barcode.isBlank();

        // 1) Buscar por SKU y BARCODE sin importar active (para detectar conflictos)
        Optional<Producto> bySku = Optional.empty();
        Optional<Producto> byBarcode = Optional.empty();

        if (hasSku) {
            bySku = productoRepository.findBySkuAndProductCategory_BusinessType_Id(sku, businessTypeId);
        }
        if (hasBarcode) {
            byBarcode = productoRepository.findByCodigoBarrasAndProductCategory_BusinessType_Id(barcode, businessTypeId);
        }

        // 2) Si ambos existen pero son productos distintos => conflicto
        if (bySku.isPresent() && byBarcode.isPresent()
                && !bySku.get().getId().equals(byBarcode.get().getId())) {
            throw new BadRequestException(
                    "Conflicto: el SKU pertenece a un producto distinto al código de barras. " +
                            "Verifica los datos antes de crear/reactivar."
            );
        }

        // 3) Si existe por BARCODE, ese manda (es el mismo producto si bySku también existe)
        if (byBarcode.isPresent()) {
            Producto p = byBarcode.get();

            if (Boolean.TRUE.equals(p.getActive())) {
                throw new IllegalArgumentException("Ya existe un producto activo con este código de barras.");
            }

            Producto saved = reactivarProducto(p, productoDTO);
            asegurarInventarioInicial(saved, sucursal, ctx);
            return productoMapper.toResponse(saved);
        }
        // 4) Si no existe por BARCODE pero sí por SKU
        if (bySku.isPresent()) {
            Producto p = bySku.get();
            if (Boolean.TRUE.equals(p.getActive())) {
                throw new IllegalArgumentException("Ya existe un producto activo con este SKU.");
            }
            Producto saved = reactivarProducto(p, productoDTO);
            asegurarInventarioInicial(saved, sucursal, ctx);
            return productoMapper.toResponse(saved);
        }

        // 5) Validaciones contra activos restantes (nombre)
        if (productoDTO.getName() != null && !productoDTO.getName().isBlank()
                && productoRepository.existsByNameIgnoreCaseAndProductCategory_BusinessType_IdAndActiveTrue(
                productoDTO.getName(), businessTypeId)) {
            throw new IllegalArgumentException("Ya existe un producto activo con este nombre.");
        }

        // 6) Crear nuevo
        Producto entity = productoMapper.toEntity(productoDTO);
        Producto saved = productoRepository.save(entity);

        asegurarInventarioInicial(saved, sucursal, ctx);
        return productoMapper.toResponse(saved);
    }
    private Producto reactivarProducto(Producto producto, ProductoDTO productoDTO) {
        productoMapper.toUpdate(productoDTO, producto.getId());
        producto.setActive(true);
        return productoRepository.save(producto);
    }
    private void asegurarInventarioInicial(Producto producto, Sucursal sucursal, UserContext ctx) {

        InventarioOwnerType ownerType = InventarioOwnerType.PROPIO;

        boolean existe = inventarioSucursalRepository
                .findByProduct_IdAndBranch_IdAndOwnerType(producto.getId(), sucursal.getId(), ownerType)
                .isPresent();

        if (existe) return;

        InventarioSucursal inv = new InventarioSucursal();
        inv.setProduct(producto);
        inv.setBranch(sucursal);
        inv.setOwnerType(ownerType);

        inv.setStock(0);
        inv.setMinStock(1);
        inv.setMaxStock(10);

        inv.setStockCritico(false);
        inv.setLastUpdatedBy(ctx.getEmail());
        inv.setLastUpdatedDate(LocalDateTime.now());

        inventarioSucursalRepository.save(inv);
    }

    public void disableProduct(Long id){
        Producto producto = productoRepository.findById(id).orElseThrow(()-> new NotFoundException("Producto no encontrado"));
        producto.setActive(false);
        productoRepository.save(producto);
    }

    @Transactional
    public ProductoResponseDTO updateProduct(ProductoDTO dto, Long id) {

        Producto entity = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        Long businessTypeId;
        if (dto.getCategoryId() != null) {
            ProductCategory pc = productCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
            businessTypeId = pc.getBusinessType().getId();
        } else {
            businessTypeId = entity.getProductCategory().getBusinessType().getId();
        }

        if (dto.getCodigoBarras() != null && !dto.getCodigoBarras().equals(entity.getCodigoBarras())) {
            if (productoRepository.existsByCodigoBarrasAndProductCategory_BusinessType_IdAndActiveTrueAndIdNot(
                    dto.getCodigoBarras(), businessTypeId, id)) {
                throw new IllegalArgumentException("Ya existe un producto activo con este código de barras.");
            }
        }

        if (dto.getSku() != null && !dto.getSku().equals(entity.getSku())) {
            if (productoRepository.existsBySkuAndProductCategory_BusinessType_IdAndActiveTrueAndIdNot(
                    dto.getSku(), businessTypeId, id)) {
                throw new IllegalArgumentException("Ya existe un producto activo con este SKU.");
            }
        }

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(entity.getName())) {
            if (productoRepository.existsByNameIgnoreCaseAndProductCategory_BusinessType_IdAndActiveTrueAndIdNot(
                    dto.getName(), businessTypeId, id)) {
                throw new IllegalArgumentException("Ya existe un producto activo con este nombre.");
            }
        }
        Producto updated = productoMapper.toUpdate(dto, id);

        Producto saved = productoRepository.save(updated);
        return productoMapper.toResponse(saved);
    }

    public Page<ProductoResponseDTO> buscarAvanzado(ProductoFiltroDTO productDTO, Pageable pageable) {
        var ctx = ctx();
        if (!ctx.isSuperAdmin()) {
            productDTO.setBusinessTypeId(ctx.getBusinessTypeId());
        }
        if (ctx.isSuperAdmin() && productDTO.getBranchId() != null) {
            Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(productDTO.getBranchId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
            Long businessTypeId = sucursal.getBusinessType().getId();
            productDTO.setBusinessTypeId(businessTypeId);
        }
        Specification<Producto> spec = ProductoSpecBuilder.fromDTO(productDTO);

        Pageable safePageable = sanitize(pageable);
        Page<Producto> page = productoRepository.findAll(spec, safePageable);

        Page<ProductoResponseDTO> dtoPage = page.map(productoMapper::toResponse);

        if (productDTO.getBranchId() == null) {
            return dtoPage;
        }

        return enrichWithStock(dtoPage, productDTO);
    }

    private Page<ProductoResponseDTO> enrichWithStock(
            Page<ProductoResponseDTO> page,
            ProductoFiltroDTO filtro
    ) {
        Page<Object[]> stockPage = productoRepository.findProductosConStock(
                filtro.getBranchId(),
                filtro.getBusinessTypeId(),
                PageRequest.of(
                        page.getNumber(),
                        page.getSize()
                )
        );

        Map<Long, Object[]> stockMap = stockPage.getContent().stream()
                .collect(Collectors.groupingBy(
                        r -> ((Producto) r[0]).getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.get(0)
                        )
                ));

        return page.map(dto -> {
            Object[] row = stockMap.get(dto.getId());
            if (row != null) {
                Integer stock = ((Number) row[1]).intValue();
                Boolean usaInventarioPorDuenio = (Boolean) row[2];

                dto.setStock(stock);
                dto.setUsaInventarioPorDuenio(usaInventarioPorDuenio);
            }
            return dto;
        });
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

    @Override
    public List<ProductoResponseDTO> getProductsByBranch(Long branchId) {
        return productoRepository.findByBranchIdAndActiveTrue(branchId)
                .stream()
                .map(productoMapper::toResponse)
                .toList();
    }

}
