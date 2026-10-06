package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_orden")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre; // Ej: EN_PREPARACION, ENTREGADO, CANCELADO

    @Column(length = 255)
    private String descripcion;
}