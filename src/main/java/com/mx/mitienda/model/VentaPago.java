package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "venta_pago")
public class VentaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "venta_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_venta_pago_venta")
    )
    private Venta venta;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payment_method_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_venta_pago_metodo_pago")
    )
    private MetodoPago paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_id",
            foreignKey = @ForeignKey(name = "fk_venta_pago_usuario")
    )
    private Usuario usuario;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}