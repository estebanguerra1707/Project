package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "compra_id", foreignKey = @ForeignKey(name ="fk_detalle_compra"))
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id", foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto producto;

    private Integer cantidad;

    private Double costoUnitario;

    private Double subtotal;

    private Boolean activo;


}
