package com.mx.mitienda.specification;

import com.mx.mitienda.model.Producto;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoSpecification {

    private ProductoSpecification() {}

    /** Evita duplicados cuando hay joins */
    public static Specification<Producto> distinct() {
        return (root, query, cb) -> { query.distinct(true); return null; };
    }

    public static Specification<Producto> businessTypeNameEquals(String name) {
        if (name == null || name.isBlank()) return null;
        String n = name.trim().toLowerCase();
        return (root, query, cb) -> cb.equal(
                cb.lower(
                        root.join("branch", JoinType.LEFT)
                                .join("businessType", JoinType.LEFT)
                                .get("name")
                ), n);
    }
    public static Specification<Producto> isActive(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Producto> nameLike(String name) {
        if (name == null || name.isBlank()) return null;
        String like = "%" + name.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
    }

    /** Mínimo (>=). OJO: atributo JPA camelCase (no "purchase_price"). */
    public static Specification<Producto> priceMajorTo(BigDecimal min) {
        if (min == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("salePrice"), min);
    }

    /** Máximo (<=) */
    public static Specification<Producto> priceMinorTo(BigDecimal max) {
        if (max == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("salePrice"), max);
    }

    /** Stock disponible: true => stock > 0, false => stock = 0 */
    public static Specification<Producto> withStockAvailable(Boolean available) {
        if (available == null) return null;

        return (Root<Producto> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // 👇 hacemos JOIN con InventarioSucursal
            Join<Object, Object> inventario = root.join("inventariosSucursal", JoinType.LEFT);

            if (available) {
                // productos con stock mayor a 0
                return cb.greaterThan(inventario.get("stock"), 0);
            } else {
                // productos con stock igual a 0 o null
                return cb.or(
                        cb.equal(inventario.get("stock"), 0),
                        cb.isNull(inventario.get("stock"))
                );
            }
        };
    }

    /**
     * Por nombre de categoría (si tu categoría es entidad).
     * Hace JOIN y compara por name case-insensitive.
     */
    public static Specification<Producto> categoryNameEquals(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return null;
        String name = categoryName.trim().toLowerCase();
        return (root, query, cb) -> {
            var cat = root.join("productCategory", JoinType.LEFT);
            // Ajusta "name" si en ProductCategory se llama distinto (p. ej. "categoryName")
            return cb.equal(cb.lower(cat.get("name")), name);
        };
    }

    /** Por ID de categoría (relación) */
    public static Specification<Producto> categoryIdEquals(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> root
                .join("productCategory", JoinType.LEFT)
                .get("id")
                .in(categoryId);
    }

    /** Sin categoría (category IS NULL) */
    public static Specification<Producto> withoutCategory(Boolean flag) {
        if (flag == null || !flag) return null;
        return (root, query, cb) -> cb.isNull(root.get("productCategory"));
    }

    /** Por ID de producto (útil en búsquedas mixtas) */
    public static Specification<Producto> idEquals(Long id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Producto> branchIdEquals(Long branchId) {
        if (branchId == null) return null;
        return (root, query, cb) -> root.join("branch", JoinType.LEFT).get("id").in(branchId);
    }

    public static Specification<Producto> businessTypeIdEquals(Long businessTypeId) {
        if (businessTypeId == null) return null;
        // Si Producto -> branch -> businessType:
        return (root, query, cb) -> root
                .join("branch", JoinType.LEFT)
                .join("businessType", JoinType.LEFT)
                .get("id").in(businessTypeId);

        // Si Producto tiene businessType directo, usa:
        // return (root, query, cb) -> root.join("businessType", JoinType.LEFT).get("id").in(businessTypeId);
    }

    // Igualdad exacta (trim)
    public static Specification<Producto> barcodeEquals(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) return null;
        String code = codigoBarras.trim();
        return (root, query, cb) -> cb.equal(root.get("codigoBarras"), code);
    }

    // Búsqueda parcial / prefijo (case-insensitive)
    public static Specification<Producto> barcodeLike(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) return null;
        String like = "%" + codigoBarras.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("codigoBarras")), like);
    }
    public static Specification<Producto> active() {
        return (root, q, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Producto> businessType(Long businessTypeId) {
        if (businessTypeId == null) return null;
        return (root, q, cb) ->
                cb.equal(root.get("productCategory").get("businessType").get("id"), businessTypeId);
    }

    /** Filtra por sucursal sólo si se provee branchId */
    public static Specification<Producto> branchIf(Long branchId) {
        if (branchId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("sucursal").get("id"), branchId);
    }

    public static Specification<Producto> byBusinessTypeAndBranch(Long businessTypeId, Long branchId, boolean superAdmin) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> ps = new ArrayList<>();

            // Solo filtra activos si NO es super admin
            if (!superAdmin) {
                ps.add(cb.isTrue(root.get("active")));
            }

            // businessType por categoría (y opcionalmente por el campo en Producto)
            if (businessTypeId != null) {
                var cat = root.join("productCategory", JoinType.LEFT);
                var btFromCat = cat.join("businessType", JoinType.LEFT).get("id");
                // Si tu modelo también guarda businessType en Producto:
                var btFromProduct = root.join("businessType", JoinType.LEFT).get("id");
                ps.add(cb.or(
                        cb.equal(btFromCat, businessTypeId),
                        cb.equal(btFromProduct, businessTypeId)
                ));
            }

            // SOLO joinear inventario si debemos filtrar por sucursal
            if (branchId != null) {
                var inv = root.join("inventariosSucursal", JoinType.INNER);
                ps.add(cb.equal(inv.get("branch").get("id"), branchId));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    public static Specification<Producto> nameOrBarcodeLike(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("codigoBarras")), like)
        );
    }

}
