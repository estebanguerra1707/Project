package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "purchase_id", foreignKey = @ForeignKey(name ="fk_detalle_compra"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Compra compra;

    @ManyToOne
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto product;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "subtotal")
    private BigDecimal subTotal;

    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type")
    private InventarioOwnerType ownerType;


}
