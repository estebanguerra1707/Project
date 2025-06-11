package com.mx.mitienda.repository;

import com.mx.mitienda.model.Cliente;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Cliente.
    Optional<Cliente> findByIdAndActivoTrue(Long id);
    List<Cliente> findByActivoTrue();

    List<Cliente> findAll(Specification clienteDTO);
}
