package com.mx.mitienda.service;


import com.mx.mitienda.model.dto.ProductoDetailDTO;
import com.mx.mitienda.model.dto.ProductoDetailResponseDTO;

public interface IProductDetailService {
    public ProductoDetailResponseDTO save(Long productoId, ProductoDetailDTO dto) ;
    public ProductoDetailResponseDTO update(Long detailId, ProductoDetailDTO dto);
    public ProductoDetailResponseDTO getProductDetail(Long productId);

    }
