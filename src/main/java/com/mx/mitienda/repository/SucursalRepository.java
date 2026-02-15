package com.mx.mitienda.repository;


import com.mx.mitienda.model.Sucursal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    Optional<Sucursal> findByIdAndActiveTrue(Long id);
    @EntityGraph(attributePaths = { "businessType" })
    @Query("""
    select s
    from Sucursal s
    join fetch s.businessType
    where s.active = true
    order by s.id
""")
    List<Sucursal> findByActiveTrueOrderByIdAsc();
    @Query("""
     select s
     from Sucursal s
     join fetch s.businessType
     where s.active = true
     order by s.id
  """)
    List<Sucursal> findAllActiveWithBusinessType();
    boolean existsByNameIgnoreCaseAndAddressIgnoreCaseAndActiveTrueAndBusinessType_Id(
            String name, String address, Long businessTypeId);
    @EntityGraph(attributePaths = { "businessType" })
    @Query("""
    select s
    from Sucursal s
    join fetch s.businessType
    where s.active = true
      and s.businessType.id = :businessTypeId
    order by s.id asc
""")
    List<Sucursal> findByBusinessType_IdAndActiveTrueOrderByIdAsc(@Param("businessTypeId") Long businessTypeId);
    List<Sucursal> findByActiveTrueOrderByNameAsc();

}
