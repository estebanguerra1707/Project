package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name = "fk_compra_proveedor"))
    private Proveedor proveedor;

    private LocalDate purchaseDate;

    private BigDecimal totalAmount;

    private Boolean active;

    @JsonManagedReference
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> details;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
