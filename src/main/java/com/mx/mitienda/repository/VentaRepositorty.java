package com.mx.mitienda.repository;

import com.mx.mitienda.model.DetalleCompra;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.Venta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepositorty extends JpaRepository<Venta, Long> {
    List<Venta> findByActivoTrue();
    Optional<Venta> findByIdAndActivoTrue(Long id);
    List<Venta> findAll(Specification<Venta> spec);
}
