package com.mx.mitienda.specification;

import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.dto.ProductCategoryFiltroDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductCategorySpecification {
    public static Specification<ProductCategory> byFilters(
            Long enforcedBusinessTypeId,
            ProductCategoryFiltroDTO filtro
    ) {
        return (root, cq, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            // businessType: si viene impuesto (no-super) lo usamos; si es super, usamos el del filtro (si hay)
            if (enforcedBusinessTypeId != null) {
                preds.add(cb.equal(root.get("businessType").get("id"), enforcedBusinessTypeId));
            } else if (filtro != null && filtro.getBusinessTypeId() != null) {
                preds.add(cb.equal(root.get("businessType").get("id"), filtro.getBusinessTypeId()));
            }

            // nombre (opcional)
            if (filtro != null && filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
                preds.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.getNombre().toLowerCase() + "%"));
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }
    public static Specification<ProductCategory> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("activo")); // o "active"
    }
}
