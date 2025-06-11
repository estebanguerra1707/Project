package com.mx.mitienda.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;

@Entity
@Data
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	private String nombre;

	private String sku;

	private String categoria;

	private String descripcion;

	private Double precio;

	private Integer stock;

	private Boolean activo;

	private LocalDate fechaCreacion;


}
