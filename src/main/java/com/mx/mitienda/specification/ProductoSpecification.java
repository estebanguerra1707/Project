package com.mx.mitienda.specification;

import com.mx.mitienda.model.Producto;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductoSpecification {
    public static Specification<Producto> isActive(Boolean active) {
        if(active== null) return null;
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("activo"),active));
    }

    public static Specification<Producto> nameLike(String name){
        if(name==null || name.isBlank()) return  null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), "%" + name.toLowerCase() +"%");
    }
    public static Specification<Producto> priceMajorTo(BigDecimal min){
        if(min ==null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("precio"), min);
    }

    public static Specification<Producto> priceMinorTo(BigDecimal max){
        if(max ==null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("precio"), max);
    }

    public static Specification<Producto>categoryEqualsTo(String category){
        if(category==null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("categoria"), category);
    }

    public static Specification<Producto> withStockAvailable(Boolean available){
        if (available == null) return null;// si es true, busca stock mayor a cero, si es false, busca con stock en cero
        return available? (root, query, cb) -> cb.greaterThan(root.get("stock"), 0)
                : (root, query, cb) -> cb.equal(root.get("stock"), 0);
    }

    public static Specification<Producto> withoutCategory(Boolean isCategory){
        if (isCategory==null || !isCategory) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("categoria"));
    }
}
