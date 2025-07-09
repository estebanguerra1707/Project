package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class InventarioSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_inventario_producto"))
    private Producto product;

    @ManyToOne
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_inventario_sucursal"))
    private Sucursal branch;
}
