package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private Boolean active;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "change_amount")
    private BigDecimal changeAmount;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private MetodoPago paymentMethod;

    @ManyToOne
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name = "fk_compra_proveedor"))
    private Proveedor proveedor;

    @JsonManagedReference
    @OneToMany(mappedBy = "compra", fetch = FetchType.LAZY , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> details;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_compra_sucursal"))
    private Sucursal branch;
}
