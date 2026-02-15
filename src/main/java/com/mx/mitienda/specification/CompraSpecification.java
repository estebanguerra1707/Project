package com.mx.mitienda.specification;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.DetalleCompra;
import com.mx.mitienda.model.Producto;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraSpecification {

    public static Specification<Compra> active(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;
            return active ? cb.isTrue(root.get("active")) : cb.isFalse(root.get("active"));
        };
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
    public static Specification<Compra> excludeIfAnyInactiveProduct() {
        return (root, query, cb) -> {
            query.distinct(true);

            // Subquery: ¿existe un detalle de esta compra cuyo producto esté inactivo?
            Subquery<Long> sub = query.subquery(Long.class);
            var dc = sub.from(DetalleCompra.class);
            var p = dc.join("product");

            sub.select(cb.literal(1L))
                    .where(
                            cb.equal(dc.get("compra"), root),  // ✅ nombre exacto en tu entity
                            cb.isFalse(p.get("active"))        // ✅ producto.active = false
                    );

            // Queremos compras donde NO exista un producto inactivo
            return cb.not(cb.exists(sub));
        };
    }
    public static Specification<Compra> byBranch(Long branchId) {
        return (root, query, cb) -> {
            if (branchId == null) return cb.conjunction();
            return cb.equal(root.get("branch").get("id"), branchId);
        };
    }
    public static Specification<Compra> byUsername(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.equal(cb.lower(root.get("usuario").get("email")), email.trim().toLowerCase());
        };
    }
    public static Specification<Compra> byUserRoles(String... roles) {
        return (root, query, cb) -> {
            if (roles == null || roles.length == 0) return null;
            return root.get("usuario").get("role").in((Object[]) roles);
        };
    }
}
