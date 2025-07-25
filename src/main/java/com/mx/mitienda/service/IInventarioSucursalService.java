package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.InventarioAlertaFiltroDTO;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IInventarioSucursalService {
    List<InventarioSucursalResponseDTO> getProductoEnSucursal(Long sucursalId, Long productId);
    List<InventarioSucursalResponseDTO> getProductosEnSucursal(Long sucursalId);
    List<InventarioSucursalResponseDTO> getProducto(Long productId);
    InventarioSucursalResponseDTO create(InventarioSucursalRequestDTO dto);
    void disminuirStock(Long productId, int cantidad);
    void aumentarStock(Long productId, int cantidad );
    Page<InventarioAlertasDTO> obtenerAlertasStock(InventarioAlertaFiltroDTO filtro, Pageable pageable);
    List<InventarioSucursalResponseDTO> getByBusinessType(Long businessTypeId);
    List<InventarioSucursalResponseDTO> findByBranchAndBusinessType();
    InventarioSucursalResponseDTO actualizarInventario(Long id, InventarioSucursalRequestDTO dto);
}