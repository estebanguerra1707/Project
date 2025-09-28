package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.util.ProductoSpecBuilder;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final IAuthenticatedUserService authenticatedUserService;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public Page<ProductoResponseDTO> getAll(Pageable pageable) {
        Usuario user = authenticatedUserService.getCurrentUser();
        Specification<Producto> spec = new ProductoSpecBuilder()
                .distinct()
                .active(true) // si quieres solo activos; quítalo si no aplica
                .build();

        if (isSuperAdmin(user)) {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new IllegalArgumentException("El usuario no tiene una sucursal asignada");
            }
            Long businessTypeId = authenticatedUserService.getBusinessTypeIdFromSession();

            spec = new ProductoSpecBuilder()
                    .distinct()
                    .active(true)
                    .inBranchId(branchId)
                    .inBusinessTypeId(businessTypeId)
                    .build();
        }
        Page<Producto> page = productoRepository.findAll(spec, pageable);
        // Nota: con paginación es mejor NO lanzar NotFound cuando viene vacío; regresa página vacía.
        return page.map(productoMapper::toResponse);
    }

    public ProductoResponseDTO getById(Long idProducto){
        Usuario user = authenticatedUserService.getCurrentUser();
        final Producto producto;
        if (isSuperAdmin(user)) {
            producto = productoRepository.findById(idProducto)
                    .filter(Producto::getActive) // si tienes campo active
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        }else{
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new IllegalArgumentException("El usuario no tiene una sucursal asignada");
            }
            producto = productoRepository
                    .findActiveWithDetailByIdAndSucursal(idProducto, branchId)
                    .orElseThrow(() -> new NotFoundException(
                            "Producto no encontrado dentro de esta sucursal asignada al usuario loggeado, intenta con otro producto"));
        }
        return productoMapper.toResponse(producto);
    }

    public ProductoResponseDTO save(ProductoDTO productoDTO){

        // Validaciones de campos obligatorios
        if (productoDTO.getName() == null || productoDTO.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (productoDTO.getSku() == null || productoDTO.getSku().isBlank()) {
            throw new IllegalArgumentException("El SKU del producto es obligatorio.");
        }
        if (productoDTO.getCodigoBarras() == null || productoDTO.getCodigoBarras().isBlank()) {
            throw new IllegalArgumentException("El código de barras es obligatorio.");
        }
        if (productoDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }

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
        Usuario user = authenticatedUserService.getCurrentUser();
        if (!isSuperAdmin(user)) {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new IllegalArgumentException("El usuario no tiene una sucursal asignada");
            }
            // asumiendo que tu DTO tiene branchId y el builder lo usa:
            productDTO.setBranchId(branchId);
        }

        Specification<Producto> spec = ProductoSpecBuilder.fromDTO(productDTO);
        Page<Producto> page = productoRepository.findAll(spec, pageable);
        return page.map(productoMapper::toResponse);
    }

    private Long getBusinessTypeIdFromSession() {
        String email = authenticatedUserService.getCurrentUser().getEmail();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return usuario.getBranch().getBusinessType().getId();
    }

    @Override
    public ProductoResponseDTO buscarPorCodigoBarras(String codigoBarras) {
        Long businessType = authenticatedUserService.getBusinessTypeIdFromSession();
        Producto producto = productoRepository.findByCodigoBarrasAndBusinessTypeId(codigoBarras, businessType)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ese código de barras"));
        return productoMapper.toResponse(producto);
    }

    private boolean isSuperAdmin(Usuario u) {
        return u != null && u.getRole() == Rol.SUPER_ADMIN; // ajusta si tu Rol es String
    }

}
