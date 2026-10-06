package com.mx.mitienda.repository;

import com.mx.mitienda.model.Gasto;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByFechaGastoBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT g.fechaGasto, SUM(g.monto) FROM Gasto g WHERE g.fechaGasto BETWEEN :startDate AND :endDate GROUP BY g.fechaGasto")
    List<Object[]> sumGastosPorDiaEnRango(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM Gasto g WHERE g.fechaGasto = :fecha AND g.sucursal.id = :branchId")
    BigDecimal sumGastosPorDiaYSucursal(@Param("fecha") LocalDate fecha, @Param("branchId") Long branchId);

    List<Gasto> findBySucursal_Id(Long sucursalId);
}