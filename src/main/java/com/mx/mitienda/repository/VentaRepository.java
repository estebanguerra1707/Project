package com.mx.mitienda.repository;

import com.mx.mitienda.model.Venta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByActiveTrue();
    Optional<Venta> findByIdAndActiveTrue(Long id);
    List<Venta> findByUsuario_UsernameAndActiveTrue(String username);
    List<Venta> findAll(Specification<Venta> spec, Sort sort);
}
