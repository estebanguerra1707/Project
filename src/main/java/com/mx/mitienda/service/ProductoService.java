package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.ProductCategoryRepository;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.ProveedorRepository;
import com.mx.mitienda.util.ProductoSpecBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ProductCategoryRepository categoryRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final ProveedorRepository proveedorRepository;

    public List<ProductoResponseDTO> getAll() {
        Stream<Producto> stream = productoRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC,"id")).stream();
        return stream.map(productoMapper::toResponse).collect(Collectors.toList());
    }

    public ProductoResponseDTO getById(Long id){
        Producto producto = productoRepository.findByIdAndActiveTrue(id).orElseThrow(()->new NotFoundException("Producto con el ID:::" +id + " no encontrado"));
        return productoMapper.toResponse(producto);
    }

    public ProductoResponseDTO save(ProductoDTO productoDTO){
        // Cargar la categoría con su businessType
        ProductCategory category = categoryRepository.findWithBusinessTypeById(productoDTO.getCategoryId())
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

      Producto producto = productoMapper.toEntity(productoDTO, category, proveedor, businessType);
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
        Producto existProduct = productoRepository.findById(id).orElseThrow(()-> new NotFoundException("Producto no encontrado"));

        if (updatedProduct.getName() != null) existProduct.setName(updatedProduct.getName());
        if (updatedProduct.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(updatedProduct.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with ID: " + updatedProduct.getCategoryId()));
            existProduct.setProductCategory(category);
        }
        if (updatedProduct.getProviderId()!=null){
            Proveedor proveedor = proveedorRepository.findById(updatedProduct.getProviderId())
                    .orElseThrow(()-> new NotFoundException( "El proveedor a actualizar no existe::" + updatedProduct.getProviderId()));
            existProduct.setProvider(proveedor);
        }
        if (updatedProduct.getDescription() != null) existProduct.setDescription(updatedProduct.getDescription());
        if (updatedProduct.getPrice() != null) existProduct.setPrice(updatedProduct.getPrice());
        if (updatedProduct.getSku() != null) existProduct.setSku(updatedProduct.getSku());
        if (updatedProduct.getStock() != null) existProduct.setStock_quantity(updatedProduct.getStock());
        if (updatedProduct.getUpdatedDate()!=null) existProduct.setCreation_date(updatedProduct.getUpdatedDate());
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



}
