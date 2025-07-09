package com.mx.mitienda.mapper;

import com.mx.mitienda.model.ProductDetail;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoDetailDTO;
import com.mx.mitienda.model.dto.ProductoDetailResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductDetailMapper {

    // ✅ Crear nuevo ProductDetail - BLINDADO
    public ProductDetail toEntity(ProductoDetailDTO productoDetailDTO, Producto producto) {
        validateCreateDTO(productoDetailDTO);

        ProductDetail detail = new ProductDetail();
        detail.setProduct(producto);
        detail.setPartNumber(productoDetailDTO.getPartNumber());
        detail.setCarBrand(productoDetailDTO.getCarBrand());
        detail.setCarModel(productoDetailDTO.getCarModel());
        detail.setYearRange(productoDetailDTO.getYearRange());
        detail.setOemEquivalent(productoDetailDTO.getOemEquivalent());
        detail.setTechnicalDescription(productoDetailDTO.getTechnicalDescription());

        return detail;
    }

    public ProductDetail toUpdate(ProductDetail existing, ProductoDetailDTO productoDetailDTO) {

        if (productoDetailDTO.getPartNumber() != null && !productoDetailDTO.getPartNumber().isBlank()) {
            existing.setPartNumber(productoDetailDTO.getPartNumber());
        }

        if (productoDetailDTO.getCarBrand() != null && !productoDetailDTO.getCarBrand().isBlank()) {
            existing.setCarBrand(productoDetailDTO.getCarBrand());
        }

        if (productoDetailDTO.getCarModel() != null && !productoDetailDTO.getCarModel().isBlank()) {
            existing.setCarModel(productoDetailDTO.getCarModel());
        }

        if (productoDetailDTO.getYearRange() != null && !productoDetailDTO.getYearRange().isBlank()) {
            existing.setYearRange(productoDetailDTO.getYearRange());
        }

        if (productoDetailDTO.getOemEquivalent() != null && !productoDetailDTO.getOemEquivalent().isBlank()) {
            existing.setOemEquivalent(productoDetailDTO.getOemEquivalent());
        }

        if (productoDetailDTO.getTechnicalDescription() != null && !productoDetailDTO.getTechnicalDescription().isBlank()) {
            existing.setTechnicalDescription(productoDetailDTO.getTechnicalDescription());
        }

        return existing;
    }


    public ProductoDetailResponseDTO toResponse(ProductDetail detail) {
        ProductoDetailResponseDTO response = new ProductoDetailResponseDTO();
        response.setId(detail.getId());
        response.setPartNumber(detail.getPartNumber());
        response.setCarBrand(detail.getCarBrand());
        response.setCarModel(detail.getCarModel());
        response.setYearRange(detail.getYearRange());
        response.setOemEquivalent(detail.getOemEquivalent());
        response.setTechnicalDescription(detail.getTechnicalDescription());
        return response;
    }

    private void validateCreateDTO(ProductoDetailDTO productoDetailDTO) {
        if (productoDetailDTO.getPartNumber() == null || productoDetailDTO.getPartNumber().isBlank()) {
            throw new IllegalArgumentException("El número de parte es obligatorio.");
        }

        if (productoDetailDTO.getCarBrand() == null || productoDetailDTO.getCarBrand().isBlank()) {
            throw new IllegalArgumentException("La marca del auto es obligatoria.");
        }
    }
}
