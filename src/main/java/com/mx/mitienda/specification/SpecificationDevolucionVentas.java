package com.mx.mitienda.specification;

import com.mx.mitienda.model.DevolucionVentas;
import com.mx.mitienda.util.enums.TipoDevolucion;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SpecificationDevolucionVentas {
    public static Specification<DevolucionVentas> hasId(Long id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<DevolucionVentas> hasVentaId(Long ventaId) {
        return (root, query, cb) ->
                ventaId == null ? null : cb.equal(root.get("venta").get("id"), ventaId);
    }

    public static Specification<DevolucionVentas> byUsername(String username) {
        return (root, query, cb) -> {
            if (username == null || username.isBlank()) return null;

            String value = username.trim().toLowerCase();

            var userJoin = root.get("usuario");

            return cb.or(
                    cb.equal(cb.lower(userJoin.get("username")), value),
                    cb.equal(cb.lower(userJoin.get("email")), value)
            );
        };
    }

    public static Specification<DevolucionVentas> byBranch(Long branchId) {
        return (root, query, cb) ->
                branchId == null ? null : cb.equal(root.get("branch").get("id"), branchId);
    }

    public static Specification<DevolucionVentas> dateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("fechaDevolucion"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("fechaDevolucion"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("fechaDevolucion"), end);
            }
            return null;
        };
    }

    public static Specification<DevolucionVentas> perDayMonthYear(Integer day, Integer month, Integer year) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (day != null) {
                predicate = cb.and(predicate,
                        cb.equal(
                                cb.function("DATE_PART", Integer.class,
                                        cb.literal("day"), root.get("fechaDevolucion")),
                                day
                        )
                );
            }

            if (month != null) {
                predicate = cb.and(predicate,
                        cb.equal(
                                cb.function("DATE_PART", Integer.class,
                                        cb.literal("month"), root.get("fechaDevolucion")),
                                month
                        )
                );
            }

            if (year != null) {
                predicate = cb.and(predicate,
                        cb.equal(
                                cb.function("DATE_PART", Integer.class,
                                        cb.literal("year"), root.get("fechaDevolucion")),
                                year
                        )
                );
            }

            return predicate;
        };
    }

    public static Specification<DevolucionVentas> montoBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("montoDevuelto"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("montoDevuelto"), min);
            } else if (max != null) {
                return cb.lessThanOrEqualTo(root.get("montoDevuelto"), max);
            }
            return null;
        };
    }

    // Cantidad devuelta por detalle (join a detalles)
    public static Specification<DevolucionVentas> cantidadBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;

            var joinDetalle = root.join("detalles", JoinType.LEFT);

            if (min != null && max != null) {
                return cb.between(joinDetalle.get("cantidadDevuelta"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(joinDetalle.get("cantidadDevuelta"), min);
            } else {
                return cb.lessThanOrEqualTo(joinDetalle.get("cantidadDevuelta"), max);
            }
        };
    }

    public static Specification<DevolucionVentas> tipoDevolucion(String tipo) {
        return (root, query, cb) -> {
            if (tipo == null || tipo.isBlank()) return null;

            TipoDevolucion tipoEnum;
            try {
                tipoEnum = TipoDevolucion.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }

            return cb.equal(root.get("tipoDevolucion"), tipoEnum);
        };
    }

    public static Specification<DevolucionVentas> byCodigoBarras(String codigo) {
        return (root, query, cb) -> {
            if (codigo == null || codigo.isBlank()) return null;
            var joinDetalle = root.join("detalles", JoinType.LEFT);
            var joinProducto = joinDetalle.join("producto", JoinType.LEFT);
            return cb.equal(joinProducto.get("codigoBarras"), codigo);
        };
    }

    public static Specification<DevolucionVentas> byProductName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            var joinDetalle = root.join("detalles", JoinType.LEFT);
            var joinProducto = joinDetalle.join("producto", JoinType.LEFT);
            return cb.like(
                    cb.lower(joinProducto.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }
}
