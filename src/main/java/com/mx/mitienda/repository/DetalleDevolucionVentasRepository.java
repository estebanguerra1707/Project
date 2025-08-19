package com.mx.mitienda.repository;

import com.mx.mitienda.model.DetalleDevolucionVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleDevolucionVentasRepository extends JpaRepository<DetalleDevolucionVentas, Long> {
    // Para validar cantidades ya devueltas de un producto en una venta
    @Query("""
        SELECT COALESCE(SUM(ddv.cantidadDevuelta), 0)
      FROM DetalleDevolucionVentas ddv
      WHERE ddv.detalleVenta.venta.id = :ventaId
        AND ddv.producto.id = :productoId
    """)
    Integer sumCantidadDevueltaPorVentaYProducto(@Param("ventaId") Long ventaId,
                                                 @Param("productoId") Long productoId);
    @Query("""
        SELECT COALESCE(SUM(ddv.cantidadDevuelta), 0)
       FROM DetalleDevolucionVentas ddv
       WHERE ddv.detalleVenta.id = :detalleVentaId
    """)
    Integer sumCantidadDevuelta(@Param("detalleVentaId") Long detalleVentaId);
}
