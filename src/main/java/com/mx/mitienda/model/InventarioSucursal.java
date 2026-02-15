package com.mx.mitienda.model;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)

public class InventarioSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal stock;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal minStock;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal maxStock;

    @Column(name = "stock_critico")
    private Boolean stockCritico = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private InventarioOwnerType ownerType = InventarioOwnerType.PROPIO;

    @LastModifiedBy
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;


    @LastModifiedDate
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_inventario_producto"))
    private Producto product;

    @ManyToOne
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_inventario_sucursal"))
    private Sucursal branch;
}
