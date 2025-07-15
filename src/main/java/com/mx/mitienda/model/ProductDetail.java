package com.mx.mitienda.model;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación: cada detalle pertenece a UN producto
    @OneToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_detail_product"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Producto product;

    private String partNumber;   // Número de parte
    private String carBrand;     // Marca del auto
    private String carModel;     // Modelo del auto
    private String yearRange;    // Ej: "2015-2020"
    private String oemEquivalent; // Equivalencia OEM
    private String technicalDescription;
}