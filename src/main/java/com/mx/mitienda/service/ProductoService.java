package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.util.ProductoSpecBuilder;
import com.mx.mitienda.util.SpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> getAll() {
        return productoRepository.findByActivoTrue();
    }

    public Producto getById(Long id){
        return productoRepository.findByIdAndActivoTrue(id).orElseThrow(()->new NotFoundException("Producto con el ID:::" +id + " no encontrado"));
    }

    public Producto save(Producto producto){
        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    public void disableProduct(Long id){
        Producto producto = getById(id);
        productoRepository.save(producto);
    }

    public Producto updateProduct(Producto updatedProduct, Long id){
        Producto existProduct = getById(id);
        existProduct.setNombre(updatedProduct.getNombre());
        existProduct.setPrecio(updatedProduct.getPrecio());
        existProduct.setDescripcion(updatedProduct.getDescripcion());
        existProduct.setStock(updatedProduct.getStock());
        existProduct.setCategoria(updatedProduct.getCategoria());
        existProduct.setSku(updatedProduct.getSku());
        productoRepository.save(existProduct);
        return existProduct;
    }

    public void logicEraseProduct(Long id){
        Producto product = getById(id);
        product.setActivo(false);
        productoRepository.save(product);
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
