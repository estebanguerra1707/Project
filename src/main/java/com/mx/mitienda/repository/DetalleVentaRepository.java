package com.mx.mitienda.repository;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVenta_Id(Long ventaId);
    /*SELECT * FROM detalle_venta
WHERE venta_id = ? AND activo = true
*/
}

