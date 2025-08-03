package com.mx.mitienda.repository;

import com.mx.mitienda.model.Venta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
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
    @Query("SELECT SUM(v.totalAmount) FROM Venta v WHERE v.saleDate BETWEEN :start AND :end AND v.active = true")
    BigDecimal obtenerGananciaPorRango(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("""
    SELECT COALESCE(SUM(v.totalAmount), 0)
    FROM Venta v
    WHERE v.saleDate BETWEEN :inicio AND :fin
      AND v.branch.id = :branchId
      AND v.active = true
""")
    BigDecimal obtenerGananciaPorRangoYBranch(@Param("inicio") LocalDateTime inicio,
                                              @Param("fin") LocalDateTime fin,
                                              @Param("branchId") Long branchId);

}
