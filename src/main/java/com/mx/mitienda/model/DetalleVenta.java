package com.mx.mitienda.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id", foreignKey = @ForeignKey(name ="fk_detalle_venta"))
    private Venta sale_id;

    @ManyToOne
    @JoinColumn(name = "producto_id", foreignKey = @ForeignKey(name ="fk_detalle_producto"))
    private Producto product_id;

    private Integer quantity;

    private Double unit_price;

    private Double subtotal;

    private Boolean active;



}
