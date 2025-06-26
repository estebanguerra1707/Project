package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cliente {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contact;

    private Boolean active = true;

    private String email;

    private String phone;

}
