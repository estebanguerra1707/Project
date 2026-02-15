package com.mx.mitienda.model;

import jakarta.persistence.*;

@Entity
public class ProductoFarmacia {
    @Id
    private Long id; // igual al ID del producto base

    @OneToOne
    @MapsId
    private Producto producto;

    @ManyToOne
    private PrincipioActivo principioActivo;

    @ManyToOne
    private FormaFarmaceutica formaFarmaceutica;

    @ManyToOne
    private UnidadMedidaEntity unidadMedida;

    private String concentracion; // ej: "500 mg"
}
