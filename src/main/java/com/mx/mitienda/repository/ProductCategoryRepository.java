package com.mx.mitienda.repository;

import com.mx.mitienda.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {
    Optional<ProductCategory> findByNameIgnoreCase(String name);
    Optional<ProductCategory> findByName(String name);
    Optional<ProductCategory> findById(Long id);
    List<ProductCategory> findByBusinessTypeId(Long businessTypeId);
    Optional<ProductCategory> findWithBusinessTypeById(Long id);
    Optional<ProductCategory> findByIdAndBusinessTypeId(Long id, Long businessTypeId);
    boolean existsByBusinessTypeIdAndNameIgnoreCase(Long businessTypeId, String name);
    boolean existsByBusinessTypeIdAndNameIgnoreCaseAndIdNot(Long businessTypeId, String name, Long id);
    List<ProductCategory> findByBusinessTypeIdAndActivoTrue(Long businessTypeId);

}
