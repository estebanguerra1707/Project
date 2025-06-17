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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String sku;

	private String category;

	private String description;

	private Double price;

	private Integer stock_quantity;

	private Boolean active;

	private LocalDate creation_date;


}
