package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "purchase_id", foreignKey = @ForeignKey(name ="fk_detalle_compra"))
    private Compra compra;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto product;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "subtotal")
    private BigDecimal subTotal;

    private Boolean active;


}
