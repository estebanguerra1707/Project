package com.mx.mitienda.specification;

import com.mx.mitienda.model.Compra;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraSpecification {

    public static Specification<Compra> active(Boolean active){
        if(active == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("activo"), active);
    }

    public static Specification<Compra> supplier(String supplier){
        if(supplier== null || supplier.isBlank()) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("proveedor")),"%" + supplier.toLowerCase() + "%");
    }

    public static Specification<Compra> dateBetween(LocalDateTime start, LocalDateTime end){
        if(start == null && end == null) return null;
        if(start!=null && end!=null)
                return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("fecha"), start, end);
        if(start !=null)
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("fecha"), start);
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("fecha"), end);
    }

    public static Specification<Compra> totalMajorTo(BigDecimal min){
        if(min ==null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("total"), min);
    }
    public static Specification<Compra> totalMinorTo(BigDecimal max){
        if(max == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("total"), max);
    }

}
