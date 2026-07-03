package com.mx.mitienda.repository;

import com.mx.mitienda.model.ClienteSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteSucursalRepository extends JpaRepository<ClienteSucursal, Long> {

    @Query("""
        select cs
        from ClienteSucursal cs
        join fetch cs.cliente c
        where cs.sucursal.id = :sucursalId
          and cs.active = true
        order by lower(coalesce(c.name, '')) asc
    """)
    List<ClienteSucursal> findBySucursalIdAndActiveTrue(@Param("sucursalId") Long sucursalId);

    @Query("""
        select cs
        from ClienteSucursal cs
        where cs.sucursal.id = :sucursalId
          and cs.cliente.id = :clienteId
    """)
    Optional<ClienteSucursal> findBySucursalIdAndClienteId(
            @Param("sucursalId") Long sucursalId,
            @Param("clienteId") Long clienteId
    );

    @Query("""
        select cs
        from ClienteSucursal cs
        where cs.sucursal.id = :sucursalId
          and cs.cliente.id = :clienteId
          and cs.active = true
    """)
    Optional<ClienteSucursal> findBySucursalIdAndClienteIdAndActiveTrue(
            @Param("sucursalId") Long sucursalId,
            @Param("clienteId") Long clienteId
    );

    @Query("""
        select count(cs)
        from ClienteSucursal cs
        where cs.cliente.id = :clienteId
          and cs.active = true
    """)
    long countByClienteIdAndActiveTrue(@Param("clienteId") Long clienteId);

    // Para evitar N+1 al armar el “multi-sucursal” en listados
    interface ClienteSucursalCount {
        Long getClienteId();
        Long getCnt();
    }

    @Query("""
        select cs.cliente.id as clienteId, cs.sucursal.id as sucursalId
        from ClienteSucursal cs
        where cs.active = true
          and cs.cliente.id in :ids
    """)
    List<Object[]> findActiveSucursalIdsByClienteIds(@Param("ids") List<Long> ids);

    @Query("""
        select cs.cliente.id as clienteId, count(cs.id) as cnt
        from ClienteSucursal cs
        where cs.active = true
          and cs.cliente.id in :ids
        group by cs.cliente.id
    """)
    List<ClienteSucursalCount> countActiveSucursalesByClienteIds(@Param("ids") List<Long> ids);

    @Query("""
        select cs
        from ClienteSucursal cs
        join fetch cs.cliente c
        where cs.active = true
        order by lower(coalesce(c.name, '')) asc
    """)
    List<ClienteSucursal> findByActiveTrue();

    @Query("""
        select cs
        from ClienteSucursal cs
        where cs.cliente.id = :clienteId
    """)
    List<ClienteSucursal> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("""
        select cs
        from ClienteSucursal cs
        where cs.cliente.id = :clienteId
          and cs.active = true
    """)
    List<ClienteSucursal> findByClienteIdAndActiveTrue(@Param("clienteId") Long clienteId);

    @Query("""
        select case when count(cs) > 0 then true else false end
        from ClienteSucursal cs
        where cs.cliente.id = :clienteId
          and cs.sucursal.id = :sucursalId
          and cs.active = true
    """)
    boolean existsByClienteIdAndSucursalIdAndActiveTrue(
            @Param("clienteId") Long clienteId,
            @Param("sucursalId") Long sucursalId
    );
}