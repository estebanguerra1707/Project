package com.mx.mitienda.repository;

import com.mx.mitienda.model.ProductCategory;
import com.mx.mitienda.model.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Producto.
    List<Producto> findByActiveTrue();
    Optional<Producto> findByIdAndActiveTrue(Long id);
    List<Producto> findAll(Specification<Producto> spec);
    List<Producto> findByActiveTrue(Sort sort);
}
