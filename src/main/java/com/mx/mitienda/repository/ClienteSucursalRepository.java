package com.mx.mitienda.repository;

import com.mx.mitienda.model.ClienteSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteSucursalRepository  extends JpaRepository<ClienteSucursal,Long> {
    List<ClienteSucursal> findBySucursalIdAndActiveTrue(Long sucursalId);

    Optional<ClienteSucursal> findBySucursalIdAndClienteId(Long sucursalId, Long clienteId);

    Optional<ClienteSucursal> findBySucursalIdAndClienteIdAndActiveTrue(Long sucursalId, Long clienteId);

    long countByClienteIdAndActiveTrue(Long clienteId);

    // Para evitar N+1 al armar el “multi-sucursal” en listados
    interface ClienteSucursalCount {
        Long getClienteId();
        Long getCnt();
    }
    @Query("""
    select cs.cliente.id as clienteId, cs.sucursal.id as sucursalId
    from ClienteSucursal cs
    where cs.active = true and cs.cliente.id in :ids
""")
    List<Object[]> findActiveSucursalIdsByClienteIds(@Param("ids") List<Long> ids);

    @Query("""
        select cs.cliente.id as clienteId, count(cs.id) as cnt
        from ClienteSucursal cs
        where cs.active = true and cs.cliente.id in :ids
        group by cs.cliente.id
    """)
    List<ClienteSucursalCount> countActiveSucursalesByClienteIds(@Param("ids") List<Long> ids);

    List<ClienteSucursal> findByActiveTrue();
    List<ClienteSucursal> findByClienteId(Long clienteId);
    List<ClienteSucursal> findByClienteIdAndActiveTrue(Long clienteId);
    boolean existsByClienteIdAndSucursalIdAndActiveTrue(Long clienteId, Long sucursalId);


}
