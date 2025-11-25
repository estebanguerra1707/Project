package com.mx.mitienda.specification;


import com.mx.mitienda.model.DevolucionCompras;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DevolucionComprasSpecification {

    public static Specification<DevolucionCompras> byId(Long id){
        return (root, q, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<DevolucionCompras> byCompra(Long compraId){
        return (root, q, cb) ->
                compraId == null ? null : cb.equal(root.get("compra").get("id"), compraId);
    }

    public static Specification<DevolucionCompras> byCodigoBarras(String codigo){
        return (root, q, cb) ->
                codigo == null ? null :
                        cb.equal(root.get("codigoBarras"), codigo);
    }

    public static Specification<DevolucionCompras> byUsuario(String username){
        return (root, q, cb) ->
                username == null ? null :
                        cb.like(cb.lower(root.get("usuario").get("username")),
                                "%" + username.toLowerCase() + "%");
    }

    public static Specification<DevolucionCompras> byTipo(String tipo){
        return (root, q, cb) ->
                tipo == null ? null :
                        cb.equal(root.get("tipoDevolucion"), tipo);
    }

    public static Specification<DevolucionCompras> dateBetween(LocalDateTime start, LocalDateTime end){
        return (root, q, cb) -> {
            if (start == null && end == null) return null;

            if (start != null && end != null)
                return cb.between(root.get("fecha"), start, end);

            return start != null
                    ? cb.greaterThanOrEqualTo(root.get("fecha"), start)
                    : cb.lessThanOrEqualTo(root.get("fecha"), end);
        };
    }

    public static Specification<DevolucionCompras> searchByDayMonthYear(Integer day, Integer month, Integer year){
        return (root, q, cb) -> {
            Predicate p = cb.conjunction();

            if (day != null)
                p = cb.and(p, cb.equal(cb.function("DAY", Integer.class, root.get("fecha")), day));

            if (month != null)
                p = cb.and(p, cb.equal(cb.function("MONTH", Integer.class, root.get("fecha")), month));

            if (year != null)
                p = cb.and(p, cb.equal(cb.function("YEAR", Integer.class, root.get("fecha")), year));

            return p;
        };
    }

    public static Specification<DevolucionCompras> montoBetween(BigDecimal min, BigDecimal max){
        return (root, q, cb) -> {
            if (min == null && max == null) return null;

            if (min != null && max != null)
                return cb.between(root.get("montoDevuelto"), min, max);

            return min != null
                    ? cb.greaterThanOrEqualTo(root.get("montoDevuelto"), min)
                    : cb.lessThanOrEqualTo(root.get("montoDevuelto"), max);
        };
    }

    public static Specification<DevolucionCompras> cantidadBetween(Integer min, Integer max){
        return (root, q, cb) -> {
            if (min == null && max == null) return null;

            if (min != null && max != null)
                return cb.between(root.get("cantidadDevuelta"), min, max);

            return min != null
                    ? cb.greaterThanOrEqualTo(root.get("cantidadDevuelta"), min)
                    : cb.lessThanOrEqualTo(root.get("cantidadDevuelta"), max);
        };
    }

    public static Specification<DevolucionCompras> branch(Long branchId){
        return (root, q, cb) ->
                branchId == null ? null :
                        cb.equal(root.get("sucursal").get("id"), branchId);
    }
}
