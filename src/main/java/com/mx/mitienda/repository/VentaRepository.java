package com.mx.mitienda.repository;

import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import io.micrometer.common.KeyValues;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {
    List<Venta> findByActiveTrue();
    Optional<Venta> findByIdAndActiveTrue(Long id);
    List<Venta> findByUsuario_UsernameAndActiveTrue(String username);
    List<Venta> findAll(Specification<Venta> spec, Sort sort);
    @Query("""
        SELECT v FROM Venta v
        JOIN v.branch b
        WHERE b.id = :branchId AND b.businessType.id = :businessTypeId
    """)
    List<Venta> findByBranchAndBusinessType(Long branchId, Long businessTypeId);
    // GANANCIA por ventas (sin devoluciones):
    // SUM( (precioVenta - precioCompra) * cantidad )
    @Query("""
        SELECT COALESCE(SUM( (dv.unitPrice - COALESCE(p.purchasePrice, 0)) * dv.quantity ), 0)
        FROM Venta v
        JOIN v.detailsList dv
        JOIN dv.product p
        WHERE v.active = true
          AND v.branch.id = :branchId
          AND v.saleDate >= :inicio
          AND v.saleDate <  :fin
    """)
    BigDecimal sumGananciaVentas(@Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin,
                                 @Param("branchId") Long branchId);
    Optional<Venta> findByIdAndBranch_IdAndActiveTrue(Long ventaId,Long  branchId);
    // VENTAS BRUTAS: usa tu totalAmount
    @Query("""
        SELECT COALESCE(SUM(v.totalAmount), 0)
        FROM Venta v
        WHERE v.active = true
          AND v.branch.id = :branchId
          AND v.saleDate >= :inicio
          AND v.saleDate <  :fin
    """)
    BigDecimal sumVentasBrutas(@Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin,
                               @Param("branchId") Long branchId);

    List<Venta>  findByBranch_IdAndActiveTrue(Long branchId);
    @Query("""
    SELECT v FROM Venta v
    LEFT JOIN FETCH v.detailsList d
    LEFT JOIN FETCH d.product p
    LEFT JOIN FETCH p.productCategory c
    LEFT JOIN FETCH p.provider pr
    WHERE v.id = :id
""")
    Optional<Venta> findByIdWithDetails(@Param("id") Long id);
}
