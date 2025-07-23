package com.mx.mitienda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class UnidadMedida {
    @Id
    @GeneratedValue
    private Long id;
    private String unidad; // mg, ml, UI, etc.
}
