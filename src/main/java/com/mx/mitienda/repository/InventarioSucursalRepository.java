package com.mx.mitienda.repository;

import com.mx.mitienda.model.InventarioSucursal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Long> {
   Optional<InventarioSucursal> findByProduct_IdAndBranch_Id(Long IdProduct, Long idBranch);
   List<InventarioSucursal> findByBranch_IdAndProduct_IdOrderByBranch_Id(Long branchId, Long productId);
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
}
