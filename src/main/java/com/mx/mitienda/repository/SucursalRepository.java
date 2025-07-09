package com.mx.mitienda.repository;


import com.mx.mitienda.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    Optional<Sucursal> findByIdAndActiveTrue(Long id);
    List<Sucursal> findByActiveTrueOrderByIdAsc();
    boolean existsByNameIgnoreCaseAndAddressIgnoreCaseAndActiveTrue(String nombre, String direccion);
}
