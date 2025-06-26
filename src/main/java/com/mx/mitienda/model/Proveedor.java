package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 100)
    private String name;

    private String contact;

    private Boolean active;

    private String email;

    @OneToMany(mappedBy = "provider")
    @JsonManagedReference
    private List<Producto> products;


}
