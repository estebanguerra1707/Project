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
        Producto producto = productoRepository.findAllActiveWithDetailOrderByIdAsc(id).orElseThrow(()->new NotFoundException("Producto no encontrado"));
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



}
