package com.mx.mitienda.repository;


import com.mx.mitienda.model.UnidadMedidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaEntity, Long> {
    Optional<UnidadMedidaEntity> findByIdAndActiveTrue(Long id);
    Optional<UnidadMedidaEntity> findByCodigoIgnoreCaseAndActiveTrue(String codigo);

}
