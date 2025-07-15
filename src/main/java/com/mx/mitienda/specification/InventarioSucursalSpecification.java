package com.mx.mitienda.specification;


import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.InventarioAlertaFiltroDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class InventarioSucursalSpecification {
    public static Specification<InventarioSucursal> conFiltros(InventarioAlertaFiltroDTO inventarioAlertaFiltroDTO){
        return (root, query, cb)->{
        Predicate predicate = cb.conjunction();
            if(inventarioAlertaFiltroDTO.getStocKCritico()!= null){
                predicate = cb.and(predicate, cb.equal(root.get("stockCritico"),inventarioAlertaFiltroDTO.getStocKCritico()));
            }
            if(inventarioAlertaFiltroDTO.getProductname()!=null && !inventarioAlertaFiltroDTO.getProductname().isBlank()){
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("product").get("name")), "%" + inventarioAlertaFiltroDTO.getProductname().toLowerCase() + "%"));
            }
            if(inventarioAlertaFiltroDTO.getBranchId()!=null){
                predicate = cb.and(predicate, cb.equal(root.get("branch").get("id"), inventarioAlertaFiltroDTO.getBranchId()));
            }
            return  predicate;
        };
    }
}
