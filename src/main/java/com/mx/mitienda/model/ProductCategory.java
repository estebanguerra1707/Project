package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "business_type_id", foreignKey = @ForeignKey(name = "fk_category_business_type"))
    private BusinessType businessType;

}

