package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
