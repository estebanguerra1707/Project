package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventarioSucursalMapper {

    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public InventarioSucursalResponseDTO toResponse(InventarioSucursal inventarioSucursal){
        InventarioSucursalResponseDTO inventarioSucursalResponseDTO = new InventarioSucursalResponseDTO();
        inventarioSucursalResponseDTO.setId(inventarioSucursal.getId());
        inventarioSucursalResponseDTO.setBranchId(inventarioSucursal.getBranch().getId());
        inventarioSucursalResponseDTO.setBranchName(inventarioSucursal.getBranch().getName());
        inventarioSucursalResponseDTO.setProductId(inventarioSucursal.getProduct().getId());
        inventarioSucursalResponseDTO.setProductName(inventarioSucursal.getProduct().getName());
        inventarioSucursalResponseDTO.setStock(inventarioSucursal.getStock());
        inventarioSucursalResponseDTO.setMinStock(inventarioSucursal.getMinStock());
        inventarioSucursalResponseDTO.setMaxStock(inventarioSucursal.getMaxStock());
        inventarioSucursalResponseDTO.setSku(inventarioSucursal.getProduct().getSku());
        inventarioSucursalResponseDTO.setLastUpdated(inventarioSucursal.getLastUpdatedDate());
        inventarioSucursalResponseDTO.setUpdatedBy(inventarioSucursal.getLastUpdatedBy());
        return inventarioSucursalResponseDTO;
    }


    public InventarioSucursal toEntity(InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        Producto producto = productoRepository.findByIdAndActiveTrue(inventarioSucursalRequestDTO.getProductId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(inventarioSucursalRequestDTO.getBranchId())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioSucursal inv = new InventarioSucursal();
        inv.setProduct(producto);
        inv.setBranch(sucursal);
        inv.setStock(inventarioSucursalRequestDTO.getQuantity());
        return inv;
    }

    public InventarioAlertasDTO toAlertDTO(InventarioSucursal inventarioSucursal){
        InventarioAlertasDTO inventarioAlertasDTO = new InventarioAlertasDTO();
        inventarioAlertasDTO.setId(inventarioSucursal.getId());
        inventarioAlertasDTO.setProductName(inventarioSucursal.getProduct().getName());
        inventarioAlertasDTO.setStock(inventarioSucursal.getStock());
        inventarioAlertasDTO.setMinStock(inventarioSucursal.getMinStock());
        inventarioAlertasDTO.setMaxStock(inventarioSucursal.getMaxStock());
        inventarioAlertasDTO.setStockCritico(inventarioSucursal.getStockCritico());
        inventarioAlertasDTO.setBranchName(inventarioSucursal.getBranch().getName());
        return inventarioAlertasDTO;
    }
}
