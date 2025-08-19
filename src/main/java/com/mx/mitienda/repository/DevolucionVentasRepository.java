package com.mx.mitienda.repository;

import com.mx.mitienda.model.DevolucionVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface DevolucionVentasRepository extends JpaRepository<DevolucionVentas, Long> {

    // IMPORTE DEVUELTO (para calcular ventas netas)
    @Query("""
    SELECT COALESCE(SUM(dv.unitPrice * ddv.cantidadDevuelta), 0)
        FROM DevolucionVentas d
        JOIN d.detalles ddv
        JOIN ddv.detalleVenta dv
        WHERE d.branch.id = :branchId
          AND d.fechaDevolucion >= :inicio
          AND d.fechaDevolucion <  :fin
    """)
    BigDecimal sumImporteDevuelto(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin,
                                  @Param("branchId") Long branchId);

    // GANANCIA PERDIDA por devoluciones (para ganancia neta)
    @Query("""
          SELECT COALESCE(SUM( (dv.unitPrice - COALESCE(p.purchasePrice, 0)) * ddv.cantidadDevuelta ), 0)
            FROM DevolucionVentas d
            JOIN d.detalles ddv
            JOIN ddv.detalleVenta dv
            JOIN dv.product p
            WHERE d.branch.id = :branchId
              AND d.fechaDevolucion >= :inicio
              AND d.fechaDevolucion <  :fin
    """)
    BigDecimal sumGananciaPerdidaPorDevoluciones(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin,
                                                 @Param("branchId") Long branchId);
}


