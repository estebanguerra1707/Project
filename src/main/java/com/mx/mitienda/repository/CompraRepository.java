package com.mx.mitienda.repository;

import com.mx.mitienda.model.Compra;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, JpaSpecificationExecutor<Compra>> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Compra.
    List<Compra> findByActiveTrueOrderByIdAsc();
    Optional<Compra> findByIdAndActiveTrue(Long id);
    List<Compra> findAll(Specification spec, Sort sort);
    List<Compra> findByUsuario_UsernameAndActiveTrue(String username);
    Optional<Compra> findById(Long id);
    @Query("""
    SELECT c FROM Compra c
    LEFT JOIN FETCH c.details d
    LEFT JOIN FETCH d.product
    LEFT JOIN FETCH c.branch
    WHERE c.id = :id AND c.active = true
""")
    Optional<Compra> findByIdWithDetails(Long id);
}
