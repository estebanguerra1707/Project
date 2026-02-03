package com.mx.mitienda.util;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.specification.CompraSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraSpecBuilder {

    private SpecificationBuilder builder = new SpecificationBuilder<>();


    public CompraSpecBuilder active(Boolean active) {
        builder.and(CompraSpecification.active(active));
        return this;
    }

    public CompraSpecBuilder supplier(Long supplierId) {
        builder.and(CompraSpecification.supplier(supplierId));
        return this;
    }

    public CompraSpecBuilder byId(Long purchaseId) {
        builder.and(CompraSpecification.byId(purchaseId));
        return this;
    }

    public CompraSpecBuilder dateBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return this;
        }

        // Normalizamos las fechas al rango completo del día
        LocalDateTime startDateTime = start;
        LocalDateTime endDateTime = end;

        builder.and(CompraSpecification.dateBetween(startDateTime, endDateTime));
        return this;
    }

    public CompraSpecBuilder totalMajorTo(BigDecimal min) {
        builder.and(CompraSpecification.totalMajorTo(min));
        return this;
    }

    public CompraSpecBuilder totalMinorTo(BigDecimal max) {
        builder.and(CompraSpecification.totalMinorTo(max));
        return this;
    }

    public CompraSpecBuilder searchPerDayMonthYear(Integer day, Integer month, Integer year) {
        builder.and(CompraSpecification.searchByDayMonthYear(day, month, year));
        return this;
    }

    public CompraSpecBuilder excludeIfAnyInactiveProduct() {
        builder.and(CompraSpecification.excludeIfAnyInactiveProduct());
        return this;
    }

    public Specification<Compra> build(){
        return builder.build();
    }

}
