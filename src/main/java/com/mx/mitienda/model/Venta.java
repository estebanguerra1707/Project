package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name ="fk_venta_cliente"))
    private Cliente cliente;

    private LocalDate fechaVenta;

    private Double totalVenta;

    private Boolean activo;

}
