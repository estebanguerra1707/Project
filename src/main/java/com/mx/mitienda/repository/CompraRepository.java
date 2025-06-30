package com.mx.mitienda.repository;

import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.Proveedor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, JpaSpecificationExecutor<Compra>> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Compra.
    List<Compra> findByActiveTrueOrderByIdAsc();
    Optional<Compra> findByIdAndActiveTrue(Long id);
    List<Compra> findAll(Specification spec);
    List<Compra> findByUsuario_UsernameAndActiveTrue(String username);
    Optional<Compra> findById(Long id);
}
