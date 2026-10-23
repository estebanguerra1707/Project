package com.mx.mitienda.repository;

import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.model.dto.UsuarioVentaResumenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Venta, Long> {

    // TOP vendidos: (producto, cantidad)
    @Query("""
        SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
            p.name,
            SUM(d.quantity)
        )
        FROM Venta v
        JOIN v.detailsList d
        JOIN d.product p
        WHERE v.active = true
          AND v.branch.id = :branchId
          AND v.saleDate BETWEEN :inicio AND :fin
        GROUP BY p.name
        ORDER BY SUM(d.quantity) DESC
    """)
    List<TopProductoDTO> topVendidos(
            @Param("branchId") Long branchId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // TOP consolidado: (producto, cantidad, ultimaVenta, categoria, tipoNegocio)
    @Query("""
  SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
    p.name,
    SUM(dv.quantity),
    MAX(v.saleDate),
    pc.name,
    bt.name,
    CAST(NULL AS string),
    CAST(NULL AS string)
  )
  FROM Venta v
  JOIN v.detailsList dv
  JOIN dv.product p
  JOIN p.productCategory pc
  JOIN pc.businessType bt
  WHERE v.branch.id = :branchId
    AND v.saleDate BETWEEN :inicio AND :fin
    AND v.active = true
  GROUP BY p.name, pc.name, bt.name
  ORDER BY SUM(dv.quantity) DESC
""")
    List<TopProductoDTO> findTopProductosConsolidado(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );

    // TOP por usuario: (producto, cantidad, ultimaVenta, username, sucursal)
    @Query("""
  SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
    p.name,
    SUM(d.quantity),
    MIN(v.saleDate),
    CAST(NULL AS string),
    CAST(NULL AS string),
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

    @Query("""
    SELECT new com.mx.mitienda.model.dto.TopProductoDTO(
        p.name,
        SUM(d.quantity),
        SUM(d.quantity * d.unitPrice),
        SUM((d.unitPrice - COALESCE(p.purchasePrice, 0)) * d.quantity),
        MAX(v.saleDate),
        pc.name,
        bt.name,
        u.id,
        u.username,
        s.name,
        COUNT(DISTINCT v.id)
    )
    FROM Venta v
    JOIN v.detailsList d
    JOIN d.product p
    JOIN p.productCategory pc
    JOIN pc.businessType bt
    JOIN v.usuario u
    JOIN v.branch s
    WHERE v.active = true
      AND d.active = true
      AND v.saleDate BETWEEN :inicio AND :fin
      AND (:branchId IS NULL OR v.branch.id = :branchId)
    GROUP BY p.name, pc.name, bt.name, u.id, u.username, s.name
    ORDER BY SUM(d.quantity) DESC
""")
    List<TopProductoDTO> findTopProductosConUsuariosDetalle(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );

    @Query("""
    SELECT new com.mx.mitienda.model.dto.UsuarioVentaResumenDTO(
        u.id,
        u.username,
        COALESCE(SUM(d.subTotal), 0),
        COALESCE(SUM((d.unitPrice - COALESCE(p.purchasePrice, 0)) * d.quantity), 0),
        COUNT(DISTINCT v.id)
    )
    FROM Venta v
    JOIN v.usuario u
    JOIN v.detailsList d
    JOIN d.product p
    WHERE v.active = true
      AND d.active = true
      AND v.saleDate >= :inicio
      AND v.saleDate < :fin
      AND (:branchId IS NULL OR v.branch.id = :branchId)
    GROUP BY u.id, u.username
    ORDER BY COALESCE(SUM((d.unitPrice - COALESCE(p.purchasePrice, 0)) * d.quantity), 0) DESC
""")
    List<UsuarioVentaResumenDTO> findVentasResumenPorUsuario(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );

    // Costo de producción y cantidad por filtro (ej. 'chilaquil' o 'pan')
    @Query("""
        SELECT COALESCE(SUM(COALESCE(p.purchasePrice, 0) * d.quantity), 0)
        FROM Venta v
        JOIN v.detailsList d
        JOIN d.product p
        LEFT JOIN p.productCategory pc
        WHERE v.active = true
          AND d.active = true
          AND v.saleDate >= :inicio
          AND v.saleDate < :fin
          AND (:branchId IS NULL OR v.branch.id = :branchId)
          AND (
              LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(pc.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    java.math.BigDecimal sumCostoProduccionPorKeyword(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId,
            @Param("keyword") String keyword
    );

    @Query("""
        SELECT COALESCE(SUM(d.quantity), 0)
        FROM Venta v
        JOIN v.detailsList d
        JOIN d.product p
        LEFT JOIN p.productCategory pc
        WHERE v.active = true
          AND d.active = true
          AND v.saleDate >= :inicio
          AND v.saleDate < :fin
          AND (:branchId IS NULL OR v.branch.id = :branchId)
          AND (
              LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(pc.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    java.math.BigDecimal sumCantidadVendidaPorKeyword(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId,
            @Param("keyword") String keyword
    );

    @Query("""
        SELECT 
            COALESCE(pc.name, 'SIN_CATEGORIA'),
            p.name,
            COALESCE(SUM(d.quantity), 0),
            COALESCE(SUM(COALESCE(p.purchasePrice, 0) * d.quantity), 0)
        FROM Venta v
        JOIN v.detailsList d
        JOIN d.product p
        LEFT JOIN p.productCategory pc
        WHERE v.active = true
          AND d.active = true
          AND v.saleDate >= :inicio
          AND v.saleDate < :fin
          AND (:branchId IS NULL OR v.branch.id = :branchId)
        GROUP BY pc.name, p.name
    """)
    List<Object[]> obtenerDesgloseCostosPorDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("branchId") Long branchId
    );
}
