package com.mx.mitienda.repository;


import com.mx.mitienda.model.DetalleDevolucionCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DetalleDevolucionComprasRepository extends JpaRepository<DetalleDevolucionCompras, Long> {

    @Query("""
        SELECT COALESCE(SUM(ddc.precioCompra * ddc.cantidadDevuelta), 0)
        FROM DevolucionCompras dc
        JOIN dc.detalles ddc
        WHERE dc.branch.id = :branchId
          AND dc.fecha >= :inicio
          AND dc.fecha <= :fin
    """)
    BigDecimal sumMontoDevueltoCompras(@Param("branchId") Long branchId,
                                       @Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin);

    @Query("""
        SELECT COALESCE(SUM(ddc.cantidadDevuelta), 0)
        FROM DetalleDevolucionCompras ddc
        WHERE ddc.detalleCompra.compra.id = :compraId
          AND ddc.detalleCompra.product.id = :productoId
    """)
    Integer sumCantidadDevueltaPorCompraYProducto(@Param("compraId") Long compraId,
                                                  @Param("productoId") Long productoId);
}
