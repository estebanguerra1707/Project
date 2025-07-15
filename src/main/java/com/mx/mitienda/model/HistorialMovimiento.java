package com.mx.mitienda.model;

import com.mx.mitienda.util.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class HistorialMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime movementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type")
    private TipoMovimiento movementType; // ENTRADA, SALIDA, AJUSTE

    private Integer quantity;

    private Integer beforeStock;

    private Integer newStock;

    private String reference; // Ej: Compra #15, Venta #8
    @ManyToOne
    @JoinColumn(name = "inventario_sucursal_id", foreignKey = @ForeignKey(name = "fk_historial_inventario"))
    private InventarioSucursal inventarioSucursal;


}
