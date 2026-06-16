package com.mx.mitienda.repository;

import com.mx.mitienda.model.VentaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface VentaPagoRepository extends JpaRepository<VentaPago, Long> {

    List<VentaPago> findByVenta_IdAndActiveTrueOrderByPaymentDateAsc(Long ventaId);
    @Query("""
    SELECT DISTINCT vp.paymentMethod.name
    FROM VentaPago vp
    WHERE vp.venta.id = :ventaId
      AND vp.active = true
""")
    List<String> findDistinctPaymentMethodNamesByVentaId(@Param("ventaId") Long ventaId);
}