package com.mx.mitienda.util;

import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.specification.ProductoSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductoSpecBuilder {
    private final SpecificationBuilder<Producto> builder = new SpecificationBuilder<>();

    /** Evita duplicados cuando haya JOINs (categoría, etc.) */
    public ProductoSpecBuilder distinct() {
        builder.and(ProductoSpecification.distinct());
        return this;
    }

    public ProductoSpecBuilder idEquals(Long id){
        builder.and(ProductoSpecification.idEquals(id));
        return this;
    }

    public ProductoSpecBuilder active(Boolean active){
        builder.and(ProductoSpecification.isActive(active));
        return this;
    }

    public ProductoSpecBuilder name(String name){
        builder.and(ProductoSpecification.nameLike(name));
        return this;
    }

    public ProductoSpecBuilder priceMajorTo(BigDecimal min){
        builder.and(ProductoSpecification.priceMajorTo(min));
        return this;
    }

    public ProductoSpecBuilder priceMinorTo(BigDecimal max){
        builder.and(ProductoSpecification.priceMinorTo(max));
        return this;
    }

    /** Conveniencia: aplica >= min y <= max, y si min>max los intercambia */
    public ProductoSpecBuilder priceBetween(BigDecimal min, BigDecimal max){
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal tmp = min; min = max; max = tmp;
        }
        builder.and(ProductoSpecification.priceMajorTo(min));
        builder.and(ProductoSpecification.priceMinorTo(max));
        return this;
    }

    /** Por nombre de categoría (case-insensitive) */
    public ProductoSpecBuilder inCategory(String category){
        builder.and(ProductoSpecification.categoryNameEquals(category));
        return this;
    }

    /** Por ID de categoría */
    public ProductoSpecBuilder inCategoryId(Long categoryId){
        builder.and(ProductoSpecification.categoryIdEquals(categoryId));
        return this;
    }

    public ProductoSpecBuilder withoutCategory(Boolean without){
        builder.and(ProductoSpecification.withoutCategory(without));     // ← usa productCategory
        return this;
    }

    /** Disponible: true => stock > 0, false => stock = 0 */
    public ProductoSpecBuilder withStockAvailable(Boolean available){
        builder.and(ProductoSpecification.withStockAvailable(available));
        return this;
    }

    public ProductoSpecBuilder inBranchId(Long branchId) {
        builder.and(ProductoSpecification.branchIdEquals(branchId));
        return this;
    }
    public ProductoSpecBuilder inBusinessTypeId(Long businessTypeId) {
        builder.and(ProductoSpecification.businessTypeIdEquals(businessTypeId));
        return this;
    }
    public ProductoSpecBuilder withBarcode(String codigoBarras) {
        builder.and(ProductoSpecification.barcodeEquals(codigoBarras)); // o barcodeLike(...)
        return this;
    }

    public ProductoSpecBuilder barcodeLike(String codigoBarras) {
        builder.and(ProductoSpecification.barcodeLike(codigoBarras));
        return this;
    }


    public Specification<Producto> build() {
        return builder.build();
    }

    /** (Opcional) fábrica desde tu DTO para dejar el service súper limpio */
    public static Specification<Producto> fromDTO(ProductoFiltroDTO dto) {
        return new ProductoSpecBuilder()
                .distinct()
                .idEquals(dto.getId())
                .active(dto.getActive())
                .name(dto.getName())
                .priceBetween(dto.getMin(), dto.getMax())
                .withStockAvailable(dto.getAvailable())
                .inCategory(dto.getCategory())
                .inCategoryId(dto.getCategoryId())
                .withoutCategory(dto.getWithoutCategory())
                .inBranchId(dto.getBranchId())
                .priceBetween(dto.getMin(), dto.getMax())
                .priceMajorTo(dto.getMin())
                .priceMinorTo(dto.getMax())
                .inBusinessTypeId(dto.getBusinessTypeId())
                .withBarcode(dto.getCodigoBarras())
                .barcodeLike(dto.getCodigoBarras())
                .build();
    }
}
