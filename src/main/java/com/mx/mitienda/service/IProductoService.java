package com.mx.mitienda.service;

import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;

import java.util.List;

public interface IProductoService {
    public List<ProductoResponseDTO> getAll();
    public ProductoResponseDTO getById(Long id);
    public ProductoResponseDTO save(ProductoDTO productoDTO);
    public void disableProduct(Long id);
    public ProductoResponseDTO updateProduct(ProductoDTO updatedProduct, Long id);
    public List<Producto> buscarAvanzado(ProductoFiltroDTO productDTO);
}
