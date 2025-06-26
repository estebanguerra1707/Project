package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String sku;

	private String description;

	private BigDecimal price;

	private Integer stock_quantity;

	private Boolean active;

	private LocalDate creation_date;
	@ManyToOne
	@JoinColumn(name = "business_type_id"
	)
	private BusinessType businessType;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_product_category"))
	private ProductCategory productCategory;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "provider_id", foreignKey = @ForeignKey(name = "fk_product_provider"))
	private Proveedor provider;

}
