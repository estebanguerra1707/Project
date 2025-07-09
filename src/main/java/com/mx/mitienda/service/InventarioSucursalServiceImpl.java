package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.InventarioSucursalMapper;
import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;
import com.mx.mitienda.repository.InventarioSucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioSucursalServiceImpl  implements IInventarioSucursalService{

    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final InventarioSucursalMapper inventarioSucursalMapper;

    @Override
    public List<InventarioSucursalResponseDTO> getProductoEnSucursal(Long sucursalId, Long productId) {
        List<InventarioSucursal> inventarioSucursalList = inventarioSucursalRepository.findByBranch_IdAndProduct_IdOrderByBranch_Id(sucursalId, productId);
        if (inventarioSucursalList.isEmpty()) {
            throw new NotFoundException("No hay inventario para este producto en esta sucursal");
        }
        return inventarioSucursalList.stream().map(inventarioSucursalMapper::toResponse).toList();
    }

    @Override
    public List<InventarioSucursalResponseDTO> getProductosEnSucursal(Long sucursalId) {
        List<InventarioSucursal> inventarioList = inventarioSucursalRepository.findByBranch_Id(sucursalId);
        return inventarioList.stream().map(inventarioSucursalMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InventarioSucursalResponseDTO> getProducto(Long productId) {
        List<InventarioSucursal> inventarioList = inventarioSucursalRepository.findByProduct_Id(productId);
        return inventarioList.stream().map(inventarioSucursalMapper::toResponse)
        .collect(Collectors.toList());
    }

    @Transactional
    public InventarioSucursalResponseDTO create(InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        InventarioSucursal inv = inventarioSucursalMapper.toEntity(inventarioSucursalRequestDTO);
        InventarioSucursal saved = inventarioSucursalRepository.save(inv);
        return inventarioSucursalMapper.toResponse(saved);
    }
}
