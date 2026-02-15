package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class DetalleDevolucionVentas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Producto producto;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal cantidadDevuelta;

    private BigDecimal precioUnitario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "detalle_venta_id", foreignKey = @ForeignKey(name = "fk_devolucion_detalle_venta"))
    private DetalleVenta detalleVenta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "devolucion_id", foreignKey = @ForeignKey(name = "fk_detalle_devolucion"))
    private DevolucionVentas devolucion;
}
