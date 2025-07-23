package com.mx.mitienda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

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

    private Integer cantidad;
}
