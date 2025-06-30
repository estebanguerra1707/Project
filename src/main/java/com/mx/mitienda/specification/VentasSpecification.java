package com.mx.mitienda.specification;

import com.mx.mitienda.model.Venta;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentasSpecification {

    public static Specification<Venta> hasClient(String nombreCliente){
        return((root, query, criteriaBuilder) ->
                nombreCliente == null?null:
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("cliente").get("nombre")), "%" + nombreCliente.toLowerCase() + "%"));
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
        return ((root, query, criteriaBuilder) -> max == null?null: criteriaBuilder.greaterThanOrEqualTo(root.get("totalVenta"), max));
    }

    public static Specification<Venta> totalMinorTo(BigDecimal min){
        return ((root, query, criteriaBuilder) -> min == null?null: criteriaBuilder.lessThanOrEqualTo(root.get("totalVenta"), min));
    }

    public static Specification<Venta> exactTotal(BigDecimal total){
        return ((root, query, criteriaBuilder) ->
                total==null?null : criteriaBuilder.equal(root.get("totalVenta"),total));
    };

    public static Specification<Venta> hasId(Long id){
        return((root, query, criteriaBuilder) ->
                id == null ? null : criteriaBuilder.equal(root.get("id"),id));
    }

    public static Specification<Venta> sellPerDay(Integer day){
        return(root, query, criteriaBuilder) -> {
            if(day ==null) return null;
            LocalDateTime since = LocalDateTime.now().minusDays(day);
            return criteriaBuilder.greaterThanOrEqualTo(root.get("fechaVenta"),day);
        };
    }

    public static Specification<Venta> sellPerYear(Integer year){
        return (root, query, criteriaBuilder) ->{
            if(year ==null) return null;

            return criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class), root.get("fechaVenta"));
        };
    }

    public static Specification<Venta> sellPerMoth(Integer month){
        return (root, query, criteriaBuilder) -> {
            if(month == null) return null;
            return criteriaBuilder.equal(criteriaBuilder.function("MONTH",Integer.class), root.get("fechaVenta"));
        };
    }

    public static Specification<Venta> isActive(Boolean active){
        if (active == null) return null;
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("activo"));
    }

    public static Specification<Venta> userName(String userName){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("usuario").get("username")),userName.toLowerCase()));
    }
}
