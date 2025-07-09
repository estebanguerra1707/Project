package com.mx.mitienda.specification;

import com.mx.mitienda.model.Compra;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraSpecification {

    public static Specification<Compra> active(Boolean active){
        if(active == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<Compra> supplier(String supplier){
        if(supplier== null || supplier.isBlank()) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("proveedor")),"%" + supplier.toLowerCase() + "%");
    }

    public static Specification<Compra> dateBetween(LocalDateTime start, LocalDateTime end){
        if(start == null && end == null) return null;
        if(start!=null && end!=null)
                return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("purchase_date"), start, end);
        if(start !=null)
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("purchase_date"), start);
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("purchase_date"), end);
    }

    public static Specification<Compra> totalMajorTo(BigDecimal min){
        if(min ==null) return null;
        //use the name of the atribute in java, not db column name
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), min);
    }
    public static Specification<Compra> totalMinorTo(BigDecimal max){
        if(max == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), max);
    }

    public static Specification<Compra> searchByDayMonthYear(Integer day, Integer month, Integer year){
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (day != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("DATE_PART", Integer.class, criteriaBuilder.literal("day"), root.get("purchaseDate")),
                                day
                        )
                );
            }

            if (month != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("DATE_PART", Integer.class, criteriaBuilder.literal("month"), root.get("purchaseDate")),
                                month
                        )
                );
            }

            if (year != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("DATE_PART", Integer.class, criteriaBuilder.literal("year"), root.get("purchaseDate")),
                                year
                        )
                );
            }
            System.out.println("SQL generado: " + criteriaBuilder.function(
                    "DATE_PART", Integer.class, criteriaBuilder.literal("day"), root.get("purchaseDate")
            ));
            return predicate;
        };
    }
}
