package com.mx.mitienda.service;

import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductoService {
    Page<ProductoResponseDTO> getAll(ProductoFiltroDTO filtro, Pageable pageable);
    ProductoResponseDTO getById(Long id);
    ProductoResponseDTO save(ProductoDTO productoDTO);
    void disableProduct(Long id);
    ProductoResponseDTO updateProduct(ProductoDTO updatedProduct, Long id);
    Page<ProductoResponseDTO> buscarAvanzado(ProductoFiltroDTO filtro, Pageable pageable);
    ProductoResponseDTO buscarPorCodigoBarras(String codigoBarras);
    List<ProductoResponseDTO> getProductsByBranch(Long branchId);
}
