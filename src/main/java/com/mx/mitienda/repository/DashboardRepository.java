package com.mx.mitienda.repository;

import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Venta, Long> {

    @Query("""
    SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
        p.name,
        SUM(d.quantity)
    )
    FROM DetalleVenta d
    JOIN d.venta v
    JOIN d.product p
    WHERE v.branch.id = :branchId
      AND v.saleDate BETWEEN :inicio AND :fin
    GROUP BY p.name
    ORDER BY SUM(d.quantity) DESC
    """)
    List<TopProductoDTO> topVendidos(
            @Param("branchId") Long branchId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );

    @Query("""
    SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
        p.name,
        SUM(d.quantity),
        MIN(v.saleDate),
        'N/A',
        s.name
    )
    FROM Venta v
    JOIN v.detailsList d
    JOIN d.product p
    JOIN v.branch s
    WHERE v.active = true
      AND v.saleDate BETWEEN :inicio AND :fin
      AND (:branchId IS NULL OR v.branch.id = :branchId)
    GROUP BY p.name, s.name
    ORDER BY SUM(d.quantity) DESC
    """)
    List<TopProductoDTO> findTopProductosConsolidado(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );

    @Query("""
    SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
        p.name,
        SUM(d.quantity),
        MIN(v.saleDate),
        u.username,
        s.name
    )
    FROM Venta v
    JOIN v.detailsList d
    JOIN d.product p
    JOIN v.usuario u
    JOIN v.branch s
    WHERE v.active = true
      AND v.saleDate BETWEEN :inicio AND :fin
      AND (:branchId IS NULL OR v.branch.id = :branchId)
    GROUP BY p.name, u.username, s.name
    ORDER BY SUM(d.quantity) DESC
    """)
    List<TopProductoDTO> findTopProductosPorUsuario(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );
}
