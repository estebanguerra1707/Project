package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_venta_extras")
public class DetalleVentaExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "detalle_venta_id", foreignKey = @ForeignKey(name ="fk_extra_detalle"))
    private DetalleVenta detalleVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_extra_id", foreignKey = @ForeignKey(name ="fk_extra_producto"))
    private Producto productExtra;

    @Column(name = "extra_price", precision = 18, scale = 3)
    private BigDecimal extraPrice;
}