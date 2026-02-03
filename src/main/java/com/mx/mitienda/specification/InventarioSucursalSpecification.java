package com.mx.mitienda.specification;


import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.InventarioAlertaFiltroDTO;
import com.mx.mitienda.model.dto.InventarioGeneralfiltroDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class InventarioSucursalSpecification {

    public static Specification<InventarioSucursal> conFiltros(InventarioAlertaFiltroDTO inventarioAlertaFiltroDTO){
        return (root, query, cb)->{
        Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate, cb.isTrue(root.get("product").get("activo")));
            if(inventarioAlertaFiltroDTO.getStocKCritico()!= null){
                predicate = cb.and(predicate, cb.equal(root.get("stockCritico"),inventarioAlertaFiltroDTO.getStocKCritico()));
            }
            if(inventarioAlertaFiltroDTO.getProductname()!=null && !inventarioAlertaFiltroDTO.getProductname().isBlank()){
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("product").get("name")), "%" + inventarioAlertaFiltroDTO.getProductname().toLowerCase() + "%"));
            }
            if(inventarioAlertaFiltroDTO.getBranchId()!=null){
                predicate = cb.and(predicate, cb.equal(root.get("branch").get("id"), inventarioAlertaFiltroDTO.getBranchId()));
            }
            if (Boolean.TRUE.equals(inventarioAlertaFiltroDTO.getUsaInventarioPorDuenio())
                    && inventarioAlertaFiltroDTO.getOwnerType() != null) {

                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("ownerType"), inventarioAlertaFiltroDTO.getOwnerType())
                );
            }

            return  predicate;
        };
    }

    public static Specification<InventarioSucursal> searchGeneral(InventarioGeneralfiltroDTO inventarioGeneralfiltroDTO){
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            // Solo productos activos
            predicate = cb.and(predicate, cb.isTrue(root.get("product").get("active")));

            // 🔹 Filtrar por sucursal
            if (inventarioGeneralfiltroDTO.getBranchId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("branch").get("id"), inventarioGeneralfiltroDTO.getBranchId()));
            }

            // 🔹 Filtrar por tipo de negocio (desde la sucursal)
            if (inventarioGeneralfiltroDTO.getBusinessTypeId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("branch").get("businessType").get("id"), inventarioGeneralfiltroDTO.getBusinessTypeId()));
            }

            // 🔹 Solo stock crítico
            if (inventarioGeneralfiltroDTO.isOnlyCritical()) {
                predicate = cb.and(predicate, cb.isTrue(root.get("stockCritico")));
            }

            // 🔹 Búsqueda por nombre o ID del producto
            if (inventarioGeneralfiltroDTO.getQ() != null && !inventarioGeneralfiltroDTO.getQ().isBlank()) {
                String like = "%" + inventarioGeneralfiltroDTO.getQ().toLowerCase() + "%";
                predicate = cb.and(predicate,
                        cb.or(
                                cb.like(cb.lower(root.get("product").get("name")), like),
                                cb.like(root.get("product").get("id").as(String.class), like)
                        ));
            }
            if (inventarioGeneralfiltroDTO.getOwnerType() != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("ownerType"), inventarioGeneralfiltroDTO.getOwnerType())
                );
            }

            // 🔹 Orden por fecha de actualización descendente
            query.orderBy(cb.desc(root.get("lastUpdatedDate")));

            return predicate;
        };

    }
}
