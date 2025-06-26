package com.mx.mitienda.repository;

import com.mx.mitienda.model.ProductCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByNameIgnoreCase(String name);
    Optional<ProductCategory> findByName(String name);
    Optional<ProductCategory> findById(Long id);
    List<ProductCategory> findByBusinessTypeId(Long businessTypeId);
    Optional<ProductCategory> findWithBusinessTypeById(Long id);

}
