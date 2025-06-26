package com.mx.mitienda.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "sale_id", foreignKey = @ForeignKey(name ="fk_detalle_venta"))
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name ="fk_detalle_producto"))
    private Producto product;

    private Integer quantity;

    private BigDecimal unit_price;

    private BigDecimal subtotal;

    private Boolean active;



}
