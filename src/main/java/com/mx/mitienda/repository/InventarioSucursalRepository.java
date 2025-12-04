package com.mx.mitienda.repository;

import com.mx.mitienda.model.InventarioSucursal;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Long>,
        JpaSpecificationExecutor<InventarioSucursal> {
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<InventarioSucursal> findByProduct_IdAndBranch_Id(Long IdProduct, Long idBranch);
    Optional<InventarioSucursal> findByBranchIdAndProductId(Long branchId, Long productId);
    List<InventarioSucursal> findByBranch_Id(Long branchId);
   List<InventarioSucursal> findByProduct_Id(Long productId);
   @Query("""
        SELECT i FROM InventarioSucursal i
        JOIN i.branch b
        JOIN i.product p
        JOIN p.productCategory pc
        JOIN pc.businessType bt
        WHERE b.id = :branchId AND bt.id = :businessTypeId
    """)
   List<InventarioSucursal> findByBranchAndBusinessType(Long branchId, Long businessTypeId);

   Page<InventarioSucursal> findAll(Specification<InventarioSucursal> spec, Pageable pageable);
   List<InventarioSucursal> findByBranch_BusinessType_Id(Long businessTypeId);
   boolean existsByIdAndBranch_Id(Long id, Long branchId);
    @Query("""
        select i
        from InventarioSucursal i
        join i.branch s
        join s.businessType bt
        join i.product p
        where
          (:branchId is null or s.id = :branchId)
          and (:businessTypeId is null or bt.id = :businessTypeId)
          and (:onlyCritical = false or i.stockCritico = true)
          and (
                :q is null
                or lower(cast(p.name as string)) like lower(concat('%', :q, '%'))
                or cast(p.id as string) like concat('%', :q, '%')
              )
        order by i.lastUpdatedDate desc
    """)
    Page<InventarioSucursal> search(
            @Param("branchId") Long branchId,
            @Param("businessTypeId") Long businessTypeId,
            @Param("q") String q,
            @Param("onlyCritical") boolean onlyCritical,
            Pageable pageable
    );
    long countByBranchIdAndStockCriticoTrue(Long branchId);
}
