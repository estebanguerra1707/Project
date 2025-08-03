package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"proveedor_id", "sucursal_id"}))
public class ProveedorSucursal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id", foreignKey = @ForeignKey(name = "fk_proveedor"))
    private Proveedor proveedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sucursal_id", foreignKey = @ForeignKey(name = "fk_sucursal"))
    private Sucursal sucursal;

}
