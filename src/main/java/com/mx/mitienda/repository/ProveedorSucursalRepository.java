package com.mx.mitienda.repository;

import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.ProveedorSucursal;
import com.mx.mitienda.model.Sucursal;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorSucursalRepository extends JpaRepository<ProveedorSucursal, Long> {

    @Query("SELECT ps.proveedor FROM ProveedorSucursal ps WHERE ps.sucursal.id = :branchId AND ps.proveedor.id = :proveedorId")
    Optional<Proveedor> findProveedorBySucursalAndProveedorId(@Param("branchId") Long branchId, @Param("proveedorId") Long proveedorId);
    boolean existsByProveedorAndSucursal(Proveedor proveedor, Sucursal sucursal);
    boolean existsByProveedorIdAndSucursalId(Long proveedorId, Long sucursalId);
    @Query("SELECT DISTINCT ps.proveedor FROM ProveedorSucursal ps WHERE ps.sucursal.id = :branchId")
    List<Proveedor> findProveedoresBySucursalId(@Param("branchId") Long branchId);
}
