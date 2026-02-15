package com.mx.mitienda.repository;

import com.mx.mitienda.model.DetalleVenta;
import com.mx.mitienda.model.dto.TopProductoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVenta_Id(Long ventaId);
    List<DetalleVenta> findByVenta_IdAndVenta_Branch_Id(Long ventaId, Long branchId);
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
         ORDER BY FUNCTION('DATE_TRUNC', 'day', v.saleDate) ASC, SUM(dv.quantity) DESC
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
      AND v.branch.id = :branchId
    GROUP BY p.name, FUNCTION('DATE_TRUNC', 'day', v.saleDate), u.username, s.name
    ORDER BY FUNCTION('DATE_TRUNC', 'day', v.saleDate) ASC, SUM(dv.quantity) DESC
""")
    List<TopProductoDTO> findTopProductosPorDiaAndSucursal(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("branchId") Long branchId
    );

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
      AND v.branch.id = :branchId
    GROUP BY p.name, FUNCTION('DATE_TRUNC', 'week', v.saleDate), u.username, s.name
    ORDER BY FUNCTION('DATE_TRUNC', 'week', v.saleDate) ASC, SUM(dv.quantity) DESC
""")
    List<TopProductoDTO> findTopProductosPorSemanaAndSucursal(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("branchId") Long branchId
    );

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
      AND v.branch.id = :branchId
    GROUP BY p.name, FUNCTION('DATE_TRUNC', 'month', v.saleDate), u.username, s.name
    ORDER BY FUNCTION('DATE_TRUNC', 'month', v.saleDate) ASC, SUM(dv.quantity) DESC
""")
    List<TopProductoDTO> findTopProductosPorMesAndSucursal(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("branchId") Long branchId
    );

    @Query("""
    SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
        p.id,
        p.name,
        SUM(d.quantity)
    )
    FROM DetalleVenta d
    JOIN d.product p
    JOIN d.venta v
    WHERE v.saleDate BETWEEN :start AND :end
      AND d.active = true
      AND v.active = true
    GROUP BY p.id, p.name
    ORDER BY SUM(d.quantity) DESC
""")
    List<TopProductoDTO> findTopVendidos(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
        p.name,
        SUM(d.quantity),
        SUM(d.quantity * d.unitPrice),
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
    List<TopProductoDTO> findTopVendidosBySucursal(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );
    @Query("""
        select dv
        from DetalleVenta dv
        join fetch dv.product p
        left join fetch p.unidadMedida um
        left join fetch p.branch b
        left join fetch p.businessType bt
        join fetch dv.venta v
        join fetch v.branch vb
        where v.id = :ventaId
    """)
    List<DetalleVenta> findFullByVentaId(@Param("ventaId") Long ventaId);

    @Query("""
        select dv
        from DetalleVenta dv
        join fetch dv.product p
        left join fetch p.unidadMedida um
        left join fetch p.branch b
        left join fetch p.businessType bt
        join fetch dv.venta v
        join fetch v.branch vb
        where v.id = :ventaId
          and v.branch.id = :branchId
    """)
    List<DetalleVenta> findFullByVentaIdAndBranchId(
            @Param("ventaId") Long ventaId,
            @Param("branchId") Long branchId
    );


}

