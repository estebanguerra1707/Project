package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name ="fk_venta_cliente"))
    private Cliente customer_id;

    private LocalDate sale_date;

    private BigDecimal total_amount;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private Boolean active;

}
