package com.mx.mitienda.repository;

import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Proveedor.
    List<Proveedor> findByActiveTrue(Sort id);

;
    @Query("""
       SELECT DISTINCT ps.proveedor
       FROM ProveedorSucursal ps
       WHERE ps.sucursal.businessType.id = :businessTypeId
       """)
    List<Proveedor> findByBusinessTypeId(@Param("businessTypeId") Long businessTypeId);

    @Query("""
       SELECT DISTINCT ps.proveedor
       FROM ProveedorSucursal ps
       WHERE ps.sucursal.id = :branchId
       """)
    List<Proveedor> findBySucursalId(@Param("branchId") Long branchId);


    List<Proveedor> findAllByActiveTrue();

    Optional<Proveedor> findByIdAndActiveTrue(Long id);

    // Para validar duplicado en UPDATE (excluyendo el mismo id)
    Optional<Proveedor> findByEmailAndNameAndIdNotAndActiveTrue(String email, String name, Long id);

    // (si lo usas en save como ya lo tienes)
    Optional<Proveedor> findByEmailAndNameAndActiveTrue(String email, String name);
    Optional<Proveedor> findByEmailAndName(String email, String name);

    @Query("""
   SELECT COUNT(ps) > 0
   FROM ProveedorSucursal ps
   WHERE ps.sucursal.id = :branchId
     AND ps.proveedor.active = true
     AND LOWER(ps.proveedor.email) = LOWER(:email)
     AND LOWER(ps.proveedor.name) = LOWER(:name)
""")
    boolean existsActiveByBranchAndEmailAndName(
            @Param("branchId") Long branchId,
            @Param("email") String email,
            @Param("name") String name
    );
    @Query("""
   SELECT DISTINCT ps.proveedor
   FROM ProveedorSucursal ps
   WHERE ps.sucursal.id = :branchId
     AND ps.sucursal.businessType.id = :businessTypeId
""")
    List<Proveedor> findBySucursalIdAndBusinessTypeId(
            @Param("branchId") Long branchId,
            @Param("businessTypeId") Long businessTypeId
    );

    @Query("""
   SELECT ps.proveedor.id
   FROM ProveedorSucursal ps
   WHERE ps.sucursal.id = :branchId
     AND ps.proveedor.active = true
     AND LOWER(ps.proveedor.email) = LOWER(:email)
     AND LOWER(ps.proveedor.name) = LOWER(:name)
""")
    List<Long> findActiveProveedorIdsByBranchAndEmailAndName(
            @Param("branchId") Long branchId,
            @Param("email") String email,
            @Param("name") String name
    );

}
