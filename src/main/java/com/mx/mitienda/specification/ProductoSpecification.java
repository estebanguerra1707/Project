package com.mx.mitienda.specification;

import com.mx.mitienda.model.Producto;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

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
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("purchasePrice"), min);
    }

    /** Máximo (<=) */
    public static Specification<Producto> priceMinorTo(BigDecimal max) {
        if (max == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("purchasePrice"), max);
    }

    /** Stock disponible: true => stock > 0, false => stock = 0 */
    public static Specification<Producto> withStockAvailable(Boolean available) {
        if (available == null) return null;
        return available
                ? (root, query, cb) -> cb.greaterThan(root.get("stock"), 0)
                : (root, query, cb) -> cb.equal(root.get("stock"), 0);
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
}
