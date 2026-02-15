package com.mx.mitienda.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class InventarioFarmacia {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Producto producto;

    @ManyToOne
    private Sucursal sucursal;

    private String lote;

    private LocalDate fechaCaducidad;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal cantidad;
}
