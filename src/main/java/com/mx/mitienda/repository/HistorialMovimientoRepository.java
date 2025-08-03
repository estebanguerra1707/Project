package com.mx.mitienda.repository;

import com.mx.mitienda.model.HistorialMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistorialMovimientoRepository extends JpaRepository<HistorialMovimiento,Long > {
    Page<HistorialMovimiento> findByInventarioSucursal_Product_IdOrderByMovementDateDesc(
            Long productId, Pageable pageable
    );
    Page<HistorialMovimiento> findByInventarioSucursal_IdOrderByMovementDateDesc(
            Long inventarioId, Pageable pageable
    );

    @Query("""
    SELECT h FROM HistorialMovimiento h
    WHERE h.inventarioSucursal.product.id = :productoId
      AND h.inventarioSucursal.branch.id = :branchId
    ORDER BY h.movementDate DESC
""")
    Page<HistorialMovimiento> findByInventarioSucursal_Product_IdAndBranch_IdOrderByMovementDateDesc(
            @Param("productoId") Long productoId,
            @Param("branchId") Long branchId,
            Pageable pageable
    );

    @Query("""
    SELECT h FROM HistorialMovimiento h
    WHERE h.inventarioSucursal.id = :inventarioId
      AND h.inventarioSucursal.branch.id = :branchId
    ORDER BY h.movementDate DESC
""")
    Page<HistorialMovimiento> findByInventarioSucursal_IdAndBranch_IdOrderByMovementDateDesc(
            @Param("inventarioId") Long inventarioId,
            @Param("branchId") Long branchId,
            Pageable pageable
    );

}
