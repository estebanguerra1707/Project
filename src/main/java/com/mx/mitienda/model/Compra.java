package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", foreignKey = @ForeignKey(name = "fk_compra_proveedor"))
    private Proveedor proveedor;

    private LocalDate purchase_date;

    private Double total_amount;

    private Boolean active;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario username;
}
