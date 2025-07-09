package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProductDetailMapper;
import com.mx.mitienda.model.ProductDetail;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoDetailDTO;
import com.mx.mitienda.model.dto.ProductoDetailResponseDTO;
import com.mx.mitienda.repository.ProductDetailRepository;
import com.mx.mitienda.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mx.mitienda.util.Utils.REFACCIONARIA_CODE;

@Service
@RequiredArgsConstructor
public class ProductoDetalleServiceImpl implements IProductDetailService{

    private final ProductDetailRepository productDetailRepository;
    private final ProductoRepository productoRepository;
    private final ProductDetailMapper productDetailMapper;

    @Transactional
    @Override
    public ProductoDetailResponseDTO save(Long productoId, ProductoDetailDTO productoDetailDTO) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        if (producto.getProductDetail() != null) {
            throw new IllegalArgumentException("Este producto ya tiene detalle técnico asignado.");
        }

        String businessTypeCode = producto.getBusinessType().getCode();
        if (!REFACCIONARIA_CODE.equalsIgnoreCase(businessTypeCode)) {
            throw new IllegalArgumentException("Solo los productos del tipo Refaccionaria pueden tener detalles técnicos.");
        }


        ProductDetail detail = productDetailMapper.toEntity(productoDetailDTO, producto);
        ProductDetail saved = productDetailRepository.save(detail);

        return productDetailMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public ProductoDetailResponseDTO update(Long detailId, ProductoDetailDTO productoDetailDTO) {
        ProductDetail existing = productDetailRepository.findById(detailId)
                .orElseThrow(() -> new NotFoundException("El detalle del producto no ha sido encontrado."));
        ProductDetail updated = productDetailMapper.toUpdate(existing, productoDetailDTO);
        ProductDetail saved = productDetailRepository.save(updated);
        return productDetailMapper.toResponse(saved);
    }

    @Override
    public ProductoDetailResponseDTO getProductDetail(Long productId) {
        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        ProductDetail detail = producto.getProductDetail();
        if (detail == null) {
            throw new NotFoundException("Este producto no tiene detalle técnico asignado");
        }
        return productDetailMapper.toResponse(detail);
    }
}
