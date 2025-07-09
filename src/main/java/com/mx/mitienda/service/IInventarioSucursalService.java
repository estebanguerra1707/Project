package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;

import java.util.List;

public interface IInventarioSucursalService {
    List<InventarioSucursalResponseDTO> getProductoEnSucursal(Long sucursalId, Long productId);
    List<InventarioSucursalResponseDTO> getProductosEnSucursal(Long sucursalId);
    List<InventarioSucursalResponseDTO> getProducto(Long productId);
    InventarioSucursalResponseDTO create(InventarioSucursalRequestDTO dto);
}