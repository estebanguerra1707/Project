package com.mx.mitienda.util;

import com.mx.mitienda.model.Producto;
import com.mx.mitienda.specification.ProductoSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductoSpecBuilder {
    private final SpecificationBuilder<Producto> builder = new SpecificationBuilder<>();

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

    public ProductoSpecBuilder inCategory(String category){
        builder.and(ProductoSpecification.categoryEqualsTo(category));
        return this;
    }

    public ProductoSpecBuilder withStockavailable(Boolean available){
        builder.and(ProductoSpecification.withStockAvailable(available));
        return this;
    }

    public ProductoSpecBuilder withoutCategory(Boolean without){
        builder.and(ProductoSpecification.withoutCategory(without));
        return this;
    }

    public Specification<Producto> build() {
        return builder.build();
    }

}
