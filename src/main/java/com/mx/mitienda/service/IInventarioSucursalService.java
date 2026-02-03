package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IInventarioSucursalService {
    InventarioSucursalResponseDTO getProductoEnSucursal(Long sucursalId, Long productId);
    List<InventarioSucursalResponseDTO> getProductosEnSucursal(Long sucursalId, InventarioOwnerType requestedOwnerType);
    List<InventarioSucursalResponseDTO> getProducto(Long productId, InventarioOwnerType requestedOwnerType);
    InventarioSucursalResponseDTO create(InventarioSucursalRequestDTO dto);
    void disminuirStock(Long productId, int cantidad, InventarioOwnerType requestedOwnerType);
    void aumentarStock(Long productId, int cantidad, InventarioOwnerType requestedOwnerType);
    Page<InventarioAlertasDTO> obtenerAlertasStock(InventarioAlertaFiltroDTO filtro, Pageable pageable);
    List<InventarioSucursalResponseDTO> getByBusinessType(Long businessTypeId);
    List<InventarioSucursalResponseDTO> findByBranchAndBusinessType();
    InventarioSucursalResponseDTO actualizarInventario(Long id, InventarioSucursalRequestDTO dto);
    Page<InventarioSucursalResponseDTO> search(
            InventarioGeneralfiltroDTO inventarioGeneralfiltroDTO,
            Pageable pageable
    );
}