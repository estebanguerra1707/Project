package com.mx.mitienda.repository;

import com.mx.mitienda.model.Cliente;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Cliente.
    Optional<Cliente> findByIdAndActiveTrue(Long id);
    List<Cliente> findByActiveTrue();
    List<Cliente> findAll(Specification clienteDTO);

    Optional<Cliente> findFirstByEmailIgnoreCase(String email);
    Optional<Cliente> findFirstByPhone(String phoneNumber);
    Optional<Cliente> findFirstByEmailIgnoreCaseAndActiveTrue(String email);
    Optional<Cliente> findFirstByPhoneAndActiveTrue(String phone);
}
