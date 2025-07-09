package com.mx.mitienda.util;

import com.mx.mitienda.model.Venta;
import com.mx.mitienda.specification.VentasSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaSpecBuilder {
    private final SpecificationBuilder<Venta> builder =  new SpecificationBuilder<>();

    public VentaSpecBuilder active(Boolean active){
        builder.and(VentasSpecification.isActive(active));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder client(String client){
        builder.and(VentasSpecification.hasClient(client));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder dateBetween(LocalDateTime start, LocalDateTime end){
        builder.and(VentasSpecification.dateBetween(start, end));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder totalMajorTo(BigDecimal min){
        builder.and(VentasSpecification.totalMajorTo(min));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }
    public VentaSpecBuilder totalMinorTo(BigDecimal max){
        builder.and(VentasSpecification.totalMinorTo(max));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder exactTotal(BigDecimal total){
        builder.and(VentasSpecification.exactTotal(total));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder sellPerDayMonthYear(Integer day, Integer month, Integer year){
        builder.and(VentasSpecification.sellPerDayMonthYear(day, month, year));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder sellPerMonthYear(Integer month, Integer year){
        builder.and(VentasSpecification.sellBetweenMonthAndYear(month, year));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }

    public VentaSpecBuilder withId(Long id){
        builder.and(VentasSpecification.hasId(id));
        return this;//al regresarlo se pueden seguir llamando metodos sobre VentaSpecBuilder
    }
    public VentaSpecBuilder username(String username) {
        if (username != null && !username.isBlank()) {
            builder.and((root, query, cb) ->
                    cb.equal(root.get("username").get("username"), username));
        }
        return this;
    }
    public VentaSpecBuilder byPaymentMethod(Long methodId){
        if(methodId!= null){
            builder.and(VentasSpecification.byPaymentMethod(methodId));
        }
        return this;
    }

    public Specification<Venta> build() {
        return builder.build();
    }

}
