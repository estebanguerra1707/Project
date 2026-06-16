package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.mx.mitienda.util.enums.EstadoPago;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_date")
    private LocalDateTime saleDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    private Boolean active;

    @Column(name = "consolidated", nullable = false)
    private Boolean consolidated = false;

    @Column(name = "weekly_ticket_id")
    private Long weeklyTicketId;

    @Column(name = "consolidated_at")
    private LocalDateTime consolidatedAt;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "change_amount")
    private BigDecimal changeAmount;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private MetodoPago paymentMethod;

    @ManyToOne
    @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name ="fk_venta_cliente"))
    private Cliente client;

    @ManyToOne
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_venta_usuario"))
    private Usuario usuario;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetalleVenta> detailsList;

    @ManyToOne
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_venta_sucursal"))
    private Sucursal branch;

    @Column(name = "total_paid", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "pending_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private EstadoPago paymentStatus = EstadoPago.PAGADA;

}
