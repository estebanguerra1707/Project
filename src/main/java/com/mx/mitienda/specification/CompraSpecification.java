package com.mx.mitienda.specification;

import com.mx.mitienda.model.Compra;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraSpecification {

    public static Specification<Compra> active(Boolean active){
        if(active == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<Compra> supplier(Long supplierId){
        if(supplierId== null) return null;
        return (Root<Compra> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                cb.equal(root.get("proveedor").get("id"), supplierId);    }

    public static Specification<Compra> byId(Long purchaseId){
        if(purchaseId==null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("id"), purchaseId);
    }
    public static Specification<Compra> dateBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return null;

        // Normalizamos los límites del rango
        LocalDateTime normalizedStart = (start != null) ? start.toLocalDate().atStartOfDay() : null;
        LocalDateTime normalizedEnd = (end != null) ? end.toLocalDate().atTime(23, 59, 59, 999_999_999) : null;

        final LocalDateTime finalStart = normalizedStart;
        final LocalDateTime finalEnd = normalizedEnd;

        if (finalStart != null && finalEnd != null) {
            return (Root<Compra> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                    cb.between(root.get("purchaseDate"), finalStart, finalEnd);
        }

        if (finalStart != null) {
            return (Root<Compra> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                    cb.greaterThanOrEqualTo(root.get("purchaseDate"), finalStart);
        }

        return (Root<Compra> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                cb.lessThanOrEqualTo(root.get("purchaseDate"), finalEnd);
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
