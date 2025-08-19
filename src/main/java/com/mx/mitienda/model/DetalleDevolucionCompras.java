package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class DetalleDevolucionCompras {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Producto producto;

    private int cantidadDevuelta;

    private BigDecimal precioCompra;

    @ManyToOne
    @JoinColumn(name = "detalle_compra_id")
    private DetalleCompra detalleCompra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "devolucion_id", foreignKey = @ForeignKey(name = "fk_detalle_devolucion_compra"))
    private DevolucionCompras devolucion;
}
