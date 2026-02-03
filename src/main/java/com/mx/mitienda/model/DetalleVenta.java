package com.mx.mitienda.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @Column(name = "unit_price") // nombre exacto de la columna en BD
    private BigDecimal unitPrice;

    @Column(name = "sub_total")
    private BigDecimal subTotal;

    @Column(name = "cantidad_devuelta")
    private Integer cantidadDevuelta;

    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type")
    private InventarioOwnerType ownerType;

    @ManyToOne
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "sale_id", foreignKey = @ForeignKey(name ="fk_detalle_venta"))
    private Venta venta;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name ="fk_detalle_producto"))
    private Producto product;

}
