package com.mx.mitienda.repository;

import com.mx.mitienda.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Proveedor.
    List<Proveedor> findByActivoTrue();
    Optional<Proveedor> findByIdAndActivoTrue(Long id);


}
