package com.mx.mitienda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class PrincipioActivo {
    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
}
