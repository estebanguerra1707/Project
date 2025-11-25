package com.mx.mitienda.repository;

import com.mx.mitienda.model.DevolucionCompras;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DevolucionComprasRepository extends JpaRepository<DevolucionCompras, Long> {
    // --- POR DÍA ---
    @Query(value = """
        SELECT CAST(d.fecha AS date) AS periodo, COUNT(*) AS total
        FROM devolucion_compras d
        WHERE d.branch_id = :branchId
          AND d.fecha >= :start
          AND d.fecha <  :end
        GROUP BY CAST(d.fecha AS date)
        ORDER BY periodo
        """, nativeQuery = true)
    List<Object[]> countPorDia(@Param("branchId") Long branchId,
                               @Param("start") LocalDateTime start,
                               @Param("end")   LocalDateTime end);

    // --- POR SEMANA (inicio de semana en LUNES) ---
    // Calcula el lunes de cada fecha y agrupa por ese "semana_inicio"
    @Query("""
  SELECT FUNCTION('date_trunc', 'week', d.fecha) AS periodo, COUNT(d)
  FROM DevolucionCompras d
  WHERE d.branch.id= :branchId
    AND d.fecha >= :start
    AND d.fecha <  :end
  GROUP BY FUNCTION('date_trunc', 'week', d.fecha)
  ORDER BY periodo
""")
    List<Object[]> countPorSemana(@Param("branchId") Long branchId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end")   LocalDateTime end);

    // --- POR MES ---
    @Query("""
  SELECT FUNCTION('date_trunc', 'month', d.fecha) AS periodo, COUNT(d)
  FROM DevolucionCompras d
  WHERE d.branch.id= :branchId
    AND d.fecha >= :start
    AND d.fecha <  :end
  GROUP BY FUNCTION('date_trunc', 'month', d.fecha)
  ORDER BY periodo
""")
    List<Object[]> countPorMes(@Param("branchId") Long branchId,
                               @Param("start") LocalDateTime start,
                               @Param("end")   LocalDateTime end);

    Page<DevolucionCompras> findAll(Specification<DevolucionCompras> spec, Pageable pageable);
}
