package com.mx.mitienda.repository;


import com.mx.mitienda.model.Sucursal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    Optional<Sucursal> findByIdAndActiveTrue(Long id);
    @EntityGraph(attributePaths = { "businessType" }) // agrega más paths si el mapper los usa
    List<Sucursal> findByActiveTrueOrderByIdAsc();
    @Query("""
     select s
     from Sucursal s
     join fetch s.businessType
     where s.active = true
     order by s.id
  """)
    List<Sucursal> findAllActiveWithBusinessType();
    boolean existsByNameIgnoreCaseAndAddressIgnoreCaseAndActiveTrue(String nombre, String direccion);
    List<Sucursal> findByBusinessType_Id(Long businessTypeId);

}
