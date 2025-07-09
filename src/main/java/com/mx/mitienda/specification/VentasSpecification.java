package com.mx.mitienda.specification;

import com.mx.mitienda.model.Venta;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class VentasSpecification {

    public static Specification<Venta> hasClient(String nombreCliente){
        return((root, query, criteriaBuilder) ->
                nombreCliente == null?null:
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("client").get("name")), "%" + nombreCliente.toLowerCase() + "%"));
    }

    public static Specification<Venta> dateBetween(LocalDateTime start, LocalDateTime end ){
        return ((root, query, criteriaBuilder) -> {
            if(start !=null && end != null) {
                return criteriaBuilder.between(root.get("saleDate"), start, end);
            }else if(start!=null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("saleDate"),start);
            }else if(end !=null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("saleDate"), end);
            }else{
                return null;
            }
        });
    }

    public static Specification<Venta> totalMajorTo(BigDecimal max){
        return ((root, query, criteriaBuilder) -> max == null?null: criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), max));
    }

    public static Specification<Venta> totalMinorTo(BigDecimal min){
        return ((root, query, criteriaBuilder) -> min == null?null: criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), min));
    }

    public static Specification<Venta> exactTotal(BigDecimal total){
        return ((root, query, criteriaBuilder) ->
                total==null?null : criteriaBuilder.equal(root.get("totalAmount"),total));
    };

    public static Specification<Venta> hasId(Long id){
        return((root, query, criteriaBuilder) ->
                id == null ? null : criteriaBuilder.equal(root.get("id"),id));
    }

    public static Specification<Venta> sellPerDayMonthYear(Integer day, Integer month, Integer year) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (day != null) {
                predicate = cb.and(predicate,
                        cb.equal(
                                cb.function("DATE_PART", Integer.class, cb.literal("day"), root.get("saleDate")),
                                day
                        )
                );
            }

            if (month != null) {
                predicate = cb.and(predicate,
                        cb.equal(
                                cb.function("DATE_PART", Integer.class, cb.literal("month"), root.get("saleDate")),
                                month
                        )
                );
            }

            if (year != null) {
                predicate = cb.and(predicate,
                        cb.equal(
                                cb.function("DATE_PART", Integer.class, cb.literal("year"), root.get("saleDate")),
                                year
                        )
                );
            }

            return predicate;
        };
    }

    public static Specification<Venta> sellBetweenMonthAndYear(Integer month, Integer year) {
        return (root, query, cb) -> {
            if (year == null || month == null) return null;

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            return cb.between(
                    root.get("saleDate"),
                    start.atStartOfDay(),
                    end.atTime(LocalTime.MAX)
            );
        };
    }

    public static Specification<Venta> isActive(Boolean active){
        if (active == null) return null;
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("active"));
    }

    public static Specification<Venta> userName(String userName){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("usuario").get("username")),userName.toLowerCase()));
    }

    public static Specification<Venta> byPaymentMethod(Long paymentMethodId) {
        return (root, query, cb) -> {
            if (paymentMethodId == null) return null;

            return cb.equal(root.get("paymentMethod").get("id"), paymentMethodId);
        };
    }
}
