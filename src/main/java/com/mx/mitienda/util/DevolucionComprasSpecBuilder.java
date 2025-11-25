package com.mx.mitienda.util;

import com.mx.mitienda.model.DevolucionCompras;
import com.mx.mitienda.specification.DevolucionComprasSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DevolucionComprasSpecBuilder {


    private SpecificationBuilder<DevolucionCompras> builder = new SpecificationBuilder<>();

    public DevolucionComprasSpecBuilder byId(Long devolucionId){
        builder.and(DevolucionComprasSpecification.byId(devolucionId));
        return this;
    }

    public DevolucionComprasSpecBuilder byCompra(Long compraId){
        builder.and(DevolucionComprasSpecification.byCompra(compraId));
        return this;
    }

    public DevolucionComprasSpecBuilder byCodigoBarras(String codigo){
        builder.and(DevolucionComprasSpecification.byCodigoBarras(codigo));
        return this;
    }

    public DevolucionComprasSpecBuilder byUsuario(String username){
        builder.and(DevolucionComprasSpecification.byUsuario(username));
        return this;
    }

    public DevolucionComprasSpecBuilder byTipo(String tipo){
        builder.and(DevolucionComprasSpecification.byTipo(tipo));
        return this;
    }

    public DevolucionComprasSpecBuilder dateBetween(LocalDateTime start, LocalDateTime end){
        builder.and(DevolucionComprasSpecification.dateBetween(start, end));
        return this;
    }

    public DevolucionComprasSpecBuilder searchPerDayMonthYear(Integer day, Integer month, Integer year){
        builder.and(DevolucionComprasSpecification.searchByDayMonthYear(day, month, year));
        return this;
    }

    public DevolucionComprasSpecBuilder montoBetween(BigDecimal min, BigDecimal max){
        builder.and(DevolucionComprasSpecification.montoBetween(min, max));
        return this;
    }

    public DevolucionComprasSpecBuilder cantidadBetween(Integer min, Integer max){
        builder.and(DevolucionComprasSpecification.cantidadBetween(min, max));
        return this;
    }

    public DevolucionComprasSpecBuilder branch(Long branchId){
        builder.and(DevolucionComprasSpecification.branch(branchId));
        return this;
    }

    public Specification<DevolucionCompras> build(){
        return builder.build();
    }
}
