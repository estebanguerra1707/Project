package com.mx.mitienda.model;

import com.mx.mitienda.util.enums.TipoDevolucion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class DevolucionVentas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaDevolucion;

    private String motivo;

    @Column(name = "monto_devuelto")
    private BigDecimal montoDevuelto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venta_id", foreignKey = @ForeignKey(name = "fk_devolucion_venta"))
    private Venta venta;


    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_devolucion_usuario"))
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_devolucion_branch"))
    private Sucursal branch;

    // Si quieres distinguir total o parcial: usa un Enum
    @Enumerated(EnumType.STRING)
    private TipoDevolucion tipoDevolucion;


    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleDevolucionVentas> detalles;
}
