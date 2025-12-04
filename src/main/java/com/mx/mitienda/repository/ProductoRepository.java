package com.mx.mitienda.repository;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Producto.
    List<Producto> findByActiveTrueAndProductCategory_BusinessType_Id(Long businessTypeId, Sort sort);
    List<Producto> findByActiveTrue();
    Optional<Producto> findByIdAndActiveTrue(Long id);
    Optional<Producto> findByCodigoBarrasAndActiveTrue(String codigo);

    List<Producto> findByActiveTrue(Sort sort);
    @Query("""
    SELECT p FROM Producto p
    LEFT JOIN FETCH p.productDetail
    WHERE p.id = :id
    AND p.active = true
    AND EXISTS (
        SELECT 1 FROM InventarioSucursal i
        WHERE i.product = p
        AND i.branch.id = :branchId
    )
""")
    Optional<Producto> findActiveWithDetailByIdAndSucursal(
            @Param("id") Long id,
            @Param("branchId") Long branchId
    );
    @Query("""
    SELECT p FROM Producto p
    JOIN p.productCategory c
    JOIN c.businessType bt
    WHERE bt.id = :businessTypeId
    AND EXISTS (
        SELECT 1 FROM InventarioSucursal i
        WHERE i.product = p AND i.branch.id = :branchId
    )
    ORDER BY p.id
""")
    List<Producto> findByBranchAndBusinessType(Long branchId, Long businessTypeId);
    boolean existsByCodigoBarrasAndProductCategory_BusinessType_Id(String codigoBarras, Long businessTypeId);
    boolean existsBySkuAndProductCategory_BusinessType_Id(String sku, Long businessTypeId);
    boolean existsByNameIgnoreCaseAndProductCategory_BusinessType_Id(String name, Long businessTypeId);
    @Query("""
    SELECT p FROM Producto p
    JOIN p.productCategory pc
    JOIN pc.businessType bt
    WHERE p.codigoBarras = :codigoBarras
      AND bt.id = :businessTypeId
      AND p.active = true
""")
    Optional<Producto> findByCodigoBarrasAndBusinessTypeId(
            @Param("codigoBarras") String codigoBarras,
            @Param("businessTypeId") Long businessTypeId
    );

    Page<Producto> findAll(Specification<Producto> spec, Pageable pageable);
    List<Producto> findByBranchIdAndActiveTrue(Long branchId);

    long countByBranchId(Long branchId);
}
