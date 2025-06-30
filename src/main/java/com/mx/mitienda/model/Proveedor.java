package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;


@Data
@ToString(exclude = {"products"})
@EqualsAndHashCode(exclude = {"products"})
@Entity
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
    @JsonBackReference
    private List<Producto> products;


}
