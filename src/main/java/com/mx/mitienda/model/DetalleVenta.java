package com.mx.mitienda.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Data
@ToString(exclude = {"venta", "product"})// no metas ni venta ni product en el toString
@EqualsAndHashCode(exclude = {"venta", "product"})//hace lo mismo pero para equals y hashCode
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @Column(name = "unit_price") // nombre exacto de la columna en BD
    private BigDecimal unitPrice;

    @Column(name = "sub_total")
    private BigDecimal subTotal;

    private Boolean active;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "sale_id", foreignKey = @ForeignKey(name ="fk_detalle_venta"))
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name ="fk_detalle_producto"))
    private Producto product;

}
