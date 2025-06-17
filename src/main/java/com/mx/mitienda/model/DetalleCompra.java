package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "compra_id", foreignKey = @ForeignKey(name ="fk_detalle_compra"))
    private Compra purchase_id;

    @ManyToOne
    @JoinColumn(name = "producto_id", foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto product_id;

    private Integer quantity;

    private Double unit_cost;

    private Double subtotal;

    private Boolean active;


}
