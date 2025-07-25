package com.mx.mitienda.repository;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.dto.TopProductoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVenta_Id(Long ventaId);

    @Query("""
        SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
            p.name,
            SUM(dv.quantity),
            SUM(dv.quantity * dv.unitPrice),
            FUNCTION('DATE_TRUNC', 'day', v.saleDate),
            u.username,
            s.name
        )
        FROM DetalleVenta dv
        JOIN dv.product p
        JOIN dv.venta v
        JOIN v.usuario u
        JOIN v.branch s
        WHERE v.saleDate BETWEEN :start AND :end
         GROUP BY p.name, FUNCTION('DATE_TRUNC', 'day', v.saleDate), u.username, s.name
         ORDER BY FUNCTION('DATE_T4
         17RUNC', 'day', v.saleDate) ASC, SUM(dv.quantity) DESC
    """)
    List<TopProductoDTO> findTopProductosPorDia(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
            p.name,
            SUM(dv.quantity),
            SUM(dv.quantity * dv.unitPrice),
            FUNCTION('DATE_TRUNC', 'week', v.saleDate),
            u.username,
            s.name
        )
        FROM DetalleVenta dv
        JOIN dv.product p
        JOIN dv.venta v
        JOIN v.usuario u
        JOIN v.branch s
        WHERE v.saleDate BETWEEN :start AND :end
        GROUP BY p.name, FUNCTION('DATE_TRUNC', 'week', v.saleDate), u.username, s.name
        ORDER BY FUNCTION('DATE_TRUNC', 'week', v.saleDate) ASC, SUM(dv.quantity) DESC
    """)
    List<TopProductoDTO> findTopProductosPorSemana(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
            p.name,
            SUM(dv.quantity),
            SUM(dv.quantity * dv.unitPrice),
            FUNCTION('DATE_TRUNC', 'month', v.saleDate),
            u.username,
            s.name
        )
        FROM DetalleVenta dv
        JOIN dv.product p
        JOIN dv.venta v
        JOIN v.usuario u
        JOIN v.branch s
        WHERE v.saleDate BETWEEN :start AND :end
        GROUP BY p.name, FUNCTION('DATE_TRUNC', 'month', v.saleDate), u.username, s.name
        ORDER BY FUNCTION('DATE_TRUNC', 'month', v.saleDate) ASC, SUM(dv.quantity) DESC
    """)
    List<TopProductoDTO> findTopProductosPorMes(LocalDateTime start, LocalDateTime end);
}

