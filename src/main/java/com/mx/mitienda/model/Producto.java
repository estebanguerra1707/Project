package com.mx.mitienda.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mx.mitienda.util.enums.UnidadMedida;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String sku;

	private String description;

	@Column(name = "purchase_price")
	private BigDecimal purchasePrice;

	@Column(name = "sale_price", precision = 18, scale = 2)
	private BigDecimal salePrice;

	private Boolean active;

	@Column(name = "creation_date")
	private LocalDateTime creationDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

	@Column(name = "codigo_barras", unique = true)
	private String codigoBarras;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_medida_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_producto_unidad_medida"))
    private UnidadMedidaEntity unidadMedida;

    @Column(nullable = false)
    private boolean permiteDecimales = false;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_product_category"))
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private ProductCategory productCategory;

	@ManyToOne
	@JsonBackReference

	@JoinColumn(name = "provider_id", foreignKey = @ForeignKey(name = "fk_product_provider"))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Proveedor provider;

	@ManyToOne
	@JoinColumn(name = "branch_id")
	private Sucursal branch;

	@ManyToOne
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@JoinColumn(name = "business_type_id", foreignKey = @ForeignKey(name = "fk_category_business_type"))
	private BusinessType businessType;

	//esto si es dce tipo refaccionaria
	@OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private ProductDetail productDetail;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private Set<InventarioSucursal> inventariosSucursal = new HashSet<>();
}
