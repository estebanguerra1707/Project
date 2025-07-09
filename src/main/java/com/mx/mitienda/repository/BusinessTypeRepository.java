package com.mx.mitienda.repository;

import com.mx.mitienda.model.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {
    List<BusinessType> findByActiveTrueOrderByIdAsc();
    Optional<BusinessType> findByIdAndActiveTrue(Long id);
}
