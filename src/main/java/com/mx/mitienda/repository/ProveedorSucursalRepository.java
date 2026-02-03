package com.mx.mitienda.repository;

import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.ProveedorSucursal;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
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
    boolean existsByProveedorIdAndSucursalId(Long proveedorId, Long sucursalId);
    @Query("SELECT DISTINCT ps.proveedor FROM ProveedorSucursal ps WHERE ps.sucursal.id = :branchId")
    List<Proveedor> findProveedoresBySucursalId(@Param("branchId") Long branchId);
    @Query("""
   SELECT new com.mx.mitienda.model.dto.ProveedorResponseDTO(
       p.id,
       p.name,
       p.contact,
       p.email,
       s.id
   )
   FROM ProveedorSucursal ps
   JOIN ps.proveedor p
   JOIN ps.sucursal s
   WHERE p.id = :proveedorId
     AND (:branchId IS NULL OR s.id = :branchId)
""")
    Optional<ProveedorResponseDTO> findProveedorForEdit(
            @Param("proveedorId") Long proveedorId,
            @Param("branchId") Long branchId
    );
    Optional<ProveedorSucursal> findFirstByProveedorIdOrderByIdDesc(Long proveedorId);


    @Query("""
        select ps.proveedor
        from ProveedorSucursal ps
        where ps.sucursal.id = :sucursalId
          and ps.proveedor.active = true
    """)
    List<Proveedor> findProveedoresActivosBySucursalId(@Param("sucursalId") Long sucursalId);

    // ya existentes:
    List<ProveedorSucursal> findByProveedorIdIn(List<Long> proveedorIds);
    List<ProveedorSucursal> findByProveedorId(Long proveedorId);
    boolean existsByProveedorAndSucursal(Proveedor proveedor, Sucursal sucursal);

}
