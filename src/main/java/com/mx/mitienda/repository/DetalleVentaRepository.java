package com.mx.mitienda.repository;

import com.mx.mitienda.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findBySellIdAndActiveTrue(Long ventaId);
}

