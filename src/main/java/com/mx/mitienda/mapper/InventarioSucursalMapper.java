package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.service.IAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InventarioSucursalMapper {

    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final IAuthenticatedUserService authenticatedUserService;

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
        inventarioSucursalResponseDTO.setIsStockCritico(inventarioSucursal.getStockCritico());
        if(inventarioSucursal.getBranch().getUsaInventarioPorDuenio()){
            inventarioSucursalResponseDTO.setOwnerType(inventarioSucursal.getOwnerType());
        }
        return inventarioSucursalResponseDTO;
    }

    public InventarioSucursal toEntity(InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        Producto producto = productoRepository.findByIdAndActiveTrue(inventarioSucursalRequestDTO.getProductId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(inventarioSucursalRequestDTO.getBranchId())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioSucursal inventarioSucursal = new InventarioSucursal();
        inventarioSucursal.setProduct(producto);
        inventarioSucursal.setMaxStock(inventarioSucursalRequestDTO.getMaxStock());
        inventarioSucursal.setMinStock(inventarioSucursalRequestDTO.getMinStock());
        inventarioSucursal.setBranch(sucursal);
        inventarioSucursalRequestDTO.setIsStockCritico(inventarioSucursalRequestDTO.getIsStockCritico());
        inventarioSucursal.setStock(inventarioSucursalRequestDTO.getQuantity());
        return inventarioSucursal;
    }
    public void updateEntity(InventarioSucursal inventarioSucursal, InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        Usuario usuario = authenticatedUserService.getCurrentUser();
        if (inventarioSucursalRequestDTO.getQuantity() != null) {
            inventarioSucursal.setStock(inventarioSucursalRequestDTO.getQuantity());
        }
        if (inventarioSucursalRequestDTO.getMinStock() != null) {
            inventarioSucursal.setMinStock(inventarioSucursalRequestDTO.getMinStock());
        }
        if (inventarioSucursalRequestDTO.getMaxStock() != null) {
            inventarioSucursal.setMaxStock(inventarioSucursalRequestDTO.getMaxStock());
        }
        if (inventarioSucursalRequestDTO.getIsStockCritico() != null) {
            inventarioSucursal.setStockCritico(inventarioSucursalRequestDTO.getIsStockCritico());
        }

        // Opcionalmente permitir actualizar la sucursal o producto si viene
        if (inventarioSucursalRequestDTO.getProductId() != null) {
            Producto producto = productoRepository.findByIdAndActiveTrue(inventarioSucursalRequestDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
            inventarioSucursal.setProduct(producto);
        }

        if (inventarioSucursalRequestDTO.getBranchId() != null) {
            Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(inventarioSucursalRequestDTO.getBranchId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
            inventarioSucursal.setBranch(sucursal);
        }
        inventarioSucursal.setLastUpdatedDate(LocalDateTime.now());
        inventarioSucursal.setLastUpdatedBy(usuario.getEmail());
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
