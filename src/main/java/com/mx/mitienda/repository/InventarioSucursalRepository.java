package com.mx.mitienda.repository;

import com.mx.mitienda.model.InventarioSucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Long> {
   Optional<InventarioSucursal> findByProduct_IdAndBranch_Id(Long IdProduct, Long idBranch);
   List<InventarioSucursal> findByBranch_IdAndProduct_IdOrderByBranch_Id(Long branchId, Long productId);
   List<InventarioSucursal> findByBranch_Id(Long branchId);
   List<InventarioSucursal> findByProduct_Id(Long productId);

}
