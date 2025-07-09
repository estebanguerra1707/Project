package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@ToString(exclude = {"provider"})
@EqualsAndHashCode(exclude = {"provider"})
@Entity
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String sku;

	private String description;

	@Column(name = "price")
	private BigDecimal purchasePrice;

	private Boolean active;

	@Column(name = "creation_date")
	private LocalDateTime creationDate;
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

	@OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private ProductDetail productDetail;

}
