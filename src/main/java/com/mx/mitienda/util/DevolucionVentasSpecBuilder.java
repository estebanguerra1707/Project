package com.mx.mitienda.util;

import com.mx.mitienda.model.DevolucionVentas;
import com.mx.mitienda.specification.SpecificationDevolucionVentas;
import com.mx.mitienda.util.enums.TipoDevolucion;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DevolucionVentasSpecBuilder {
    private final SpecificationBuilder<DevolucionVentas> builder = new SpecificationBuilder<>();

    public DevolucionVentasSpecBuilder withId(Long id) {
        builder.and(SpecificationDevolucionVentas.hasId(id));
        return this;
    }

    public DevolucionVentasSpecBuilder ventaId(Long ventaId) {
        builder.and(SpecificationDevolucionVentas.hasVentaId(ventaId));
        return this;
    }

    public DevolucionVentasSpecBuilder username(String username) {
        builder.and(SpecificationDevolucionVentas.byUsername(username));
        return this;
    }

    public DevolucionVentasSpecBuilder branch(Long branchId) {
        builder.and(SpecificationDevolucionVentas.byBranch(branchId));
        return this;
    }

    public DevolucionVentasSpecBuilder dateBetween(LocalDateTime start, LocalDateTime end) {
        builder.and(SpecificationDevolucionVentas.dateBetween(start, end));
        return this;
    }

    public DevolucionVentasSpecBuilder perDayMonthYear(Integer day, Integer month, Integer year) {
        builder.and(SpecificationDevolucionVentas.perDayMonthYear(day, month, year));
        return this;
    }

    public DevolucionVentasSpecBuilder montoBetween(BigDecimal min, BigDecimal max) {
        builder.and(SpecificationDevolucionVentas.montoBetween(min, max));
        return this;
    }

    public DevolucionVentasSpecBuilder cantidadBetween(Integer min, Integer max) {
        builder.and(SpecificationDevolucionVentas.cantidadBetween(min, max));
        return this;
    }

    public DevolucionVentasSpecBuilder tipoDevolucion(String tipo) {
            builder.and(SpecificationDevolucionVentas.tipoDevolucion(tipo));
        return this;
    }

    public DevolucionVentasSpecBuilder codigoBarras(String codigo) {
        builder.and(SpecificationDevolucionVentas.byCodigoBarras(codigo));
        return this;
    }

    public DevolucionVentasSpecBuilder productName(String name) {
        builder.and(SpecificationDevolucionVentas.byProductName(name));
        return this;
    }

    public Specification<DevolucionVentas> build() {
        return builder.build();
    }
}
