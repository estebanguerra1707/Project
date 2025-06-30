package com.mx.mitienda.util;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.specification.CompraSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraSpecBuilder {

    private SpecificationBuilder builder = new SpecificationBuilder<>();


    public CompraSpecBuilder active(Boolean active){
        builder.and(CompraSpecification.active(active));
        return this;
    }

    public CompraSpecBuilder supplier(String supplier){
        builder.and(CompraSpecification.supplier(supplier));
        return this;
    }

    public CompraSpecBuilder dateBetween(LocalDateTime start, LocalDateTime end){
        builder.and(CompraSpecification.dateBetween(start, end));
        return this;
    }

    public CompraSpecBuilder totalMajorTo(BigDecimal min){
        builder.and(CompraSpecification.totalMajorTo(min));
        return this;
    }
    public CompraSpecBuilder totalMinorTo(BigDecimal max){
        builder.and(CompraSpecification.totalMinorTo(max));
        return this;
    }

    public Specification<Compra> build(){
        return builder.build();
    }
}
