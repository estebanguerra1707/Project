package com.mx.mitienda.repository;

import com.mx.mitienda.model.Compra;
import io.micrometer.common.KeyValues;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, JpaSpecificationExecutor<Compra>> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Compra.
    List<Compra> findByActiveTrueOrderByIdAsc();
    Optional<Compra> findByIdAndActiveTrue(Long id);
    Optional<Compra> findByIdAndBranch_IdAndActiveTrue(Long id, Long branchId);
    @Query("""
SELECT DISTINCT c FROM Compra c
LEFT JOIN FETCH c.details d
WHERE (:#{#spec} IS NULL OR :#{#spec} = :#{#spec})
""")
    List<Compra> findAllWithDetails(Specification<Compra> spec, Sort sort);    List<Compra> findByUsuario_UsernameAndActiveTrue(String username);
    Optional<Compra> findById(Long id);
    @Query("""
    SELECT c FROM Compra c
    LEFT JOIN FETCH c.details d
    LEFT JOIN FETCH d.product
    LEFT JOIN FETCH c.branch
    WHERE c.id = :id AND c.active = true
""")
    Optional<Compra> findByIdWithDetails(Long id);

    @Query("""
        SELECT c FROM Compra c
        JOIN c.branch b
        WHERE b.id = :branchId AND b.businessType.id = :businessTypeId
    """)
    List<Compra> findByBranchAndBusinessType(Long branchId, Long businessTypeId);

    List<Compra> findByBranch_IdAndActiveTrue(Long branchId);
    Page<Compra> findByBranchIdAndActiveTrue(Long branchId, Pageable pageable);

    Page<Compra> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"details"})
    @Query("SELECT c FROM Compra c WHERE c.active = true ORDER BY c.purchaseDate DESC")
    Page<Compra> findAllWithDetails(Pageable pageable);

    @EntityGraph(attributePaths = {"details", "proveedor", "paymentMethod"})
    Page<Compra> findAll(Specification<Compra> spec, Pageable pageable);
}
