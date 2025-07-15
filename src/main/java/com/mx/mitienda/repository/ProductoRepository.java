package com.mx.mitienda.repository;

import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Producto;
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
    List<Producto> findByActiveTrue();
    Optional<Producto> findByIdAndActiveTrue(Long id);
    List<Producto> findAll(Specification<Producto> spec);
    List<Producto> findByActiveTrue(Sort sort);
    @Query("""
    SELECT DISTINCT p FROM Producto p
    LEFT JOIN FETCH p.productDetail
    WHERE p.id = :id AND p.active = true
    ORDER BY p.id ASC
""")
    Optional<Producto> findAllActiveWithDetailOrderByIdAsc(@Param("id") Long id);
    @Query("""
        SELECT p FROM Producto p
        JOIN p.productCategory c
        JOIN c.businessType bt
        WHERE p.branch.id = :branchId AND bt.id = :businessTypeId
    """)
    List<Producto> findByBranchAndBusinessType(Long branchId, Long businessTypeId);
}
