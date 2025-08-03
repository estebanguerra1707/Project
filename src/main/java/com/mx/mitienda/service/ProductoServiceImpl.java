package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.util.ProductoSpecBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public List<Producto> buscarAvanzado(ProductoFiltroDTO productDTO) {
        Specification<Producto> spec = new ProductoSpecBuilder()
                .active(productDTO.getActive())
                .name(productDTO.getName())
                .priceMajorTo(productDTO.getMin())
                .priceMinorTo(productDTO.getMax())
                .withStockavailable(productDTO.getAvailabe())
                .inCategory(productDTO.getCategory())
                .withoutCategory(productDTO.getWithoutCategory())
                .build();
        return productoRepository.findAll(spec);
    }


    private Long getBusinessTypeIdFromSession() {
        String email = authenticatedUserService.getCurrentUser().getEmail();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return usuario.getBranch().getBusinessType().getId();
    }

}
