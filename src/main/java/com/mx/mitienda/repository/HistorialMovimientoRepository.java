package com.mx.mitienda.repository;

import com.mx.mitienda.model.HistorialMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HistorialMovimientoRepository extends JpaRepository<HistorialMovimiento,Long > {
    Page<HistorialMovimiento> findByInventarioSucursal_Product_IdOrderByMovementDateDesc(
            Long productId, Pageable pageable
    );
    Page<HistorialMovimiento> findByInventarioSucursal_IdOrderByMovementDateDesc(
            Long inventarioId, Pageable pageable
    );
}
