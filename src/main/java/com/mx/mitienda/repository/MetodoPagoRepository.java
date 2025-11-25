package com.mx.mitienda.repository;

import com.mx.mitienda.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    List<MetodoPago> findByActivoTrue();
    Optional<MetodoPago> findByIdAndActivoTrue(Long id);
}
