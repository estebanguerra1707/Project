package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Boolean active;
    @Column(name = "alerta_stock_critico")
    private Boolean alertaStockCritico;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_type_id", foreignKey = @ForeignKey(name = "fk_sucursal_business_type"))
    private BusinessType businessType;

   //Sucursal sepa sus usuarios
    @OneToMany(mappedBy = "branch")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Usuario> usuarios;
}
