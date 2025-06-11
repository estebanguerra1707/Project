package com.mx.mitienda.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "venta_id", foreignKey = @ForeignKey(name ="fk_detalle_venta"))
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id", foreignKey = @ForeignKey(name ="fk_detalle_producto"))
    private Producto producto;

    private Integer cantidad;

    private Double precioUnitario;

    private Double subtotal;

    private Boolean activo;



}
