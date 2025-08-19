package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.util.ProductoSpecBuilder;
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

    public List<ProductoResponseDTO> getAll() {
        Long branchId = authenticatedUserService.getCurrentBranchId();
        log.info("BRANCH DEL USUARIO::" + branchId);
        Long businessTypeId = authenticatedUserService.getBusinessTypeIdFromSession();
        log.info("NEGOCIO DEL USUARIO::" + businessTypeId);
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId).orElseThrow(()-> new NotFoundException("No se ha encontado la sucursal"));
        BusinessType businessType = businessTypeRepository.findByIdAndActiveTrue(businessTypeId).orElseThrow(()-> new NotFoundException("No se ha encontrado el tipo de negocio"));
        List<Producto> productos = productoRepository.findByBranchAndBusinessType(branchId, businessTypeId);
        if (productos.isEmpty()) {
            throw new NotFoundException(String.format(
                    "No hay productos disponibles para la sucursal '%s' con tipo de negocio '%s'",
                    sucursal.getName(),
                    businessType.getName()));
        }
        return productos.stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductoResponseDTO getById(Long idProducto){
        Long branchId = authenticatedUserService.getCurrentBranchId();
        Producto producto = productoRepository.findActiveWithDetailByIdAndSucursal(idProducto,branchId ).orElseThrow(()->new NotFoundException("Producto no encontrado dentro de esta sucursal asignada al usuario loggeado, intenta con otro producto"));
        return productoMapper.toResponse(producto);
    }

    public ProductoResponseDTO save(ProductoDTO productoDTO){

      Producto producto = productoMapper.toEntity(productoDTO);

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

        Producto saved = productoRepository.save(producto);
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
        Usuario usuario = authenticatedUserService.getCurrentUser();
        Long businessType = authenticatedUserService.getBusinessTypeIdFromSession();
        Producto producto = productoRepository.findByCodigoBarrasAndBusinessTypeId(codigoBarras, businessType)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ese código de barras"));
        return productoMapper.toResponse(producto);
    }

}
