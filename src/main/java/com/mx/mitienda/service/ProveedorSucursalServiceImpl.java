package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProveedorMapper;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.repository.ProveedorSucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedorSucursalServiceImpl implements  IProveedorSucursalService{

    private final ProveedorSucursalRepository proveedorSucursalRepository;
    private final ProveedorMapper proveedorMapper;


    @Override
    public List<ProveedorResponseDTO> getProveedoresBySucursal(Long branchId) {
        List<Proveedor> proveedores = proveedorSucursalRepository.findProveedoresBySucursalId(branchId);
        return proveedores.stream().map(proveedorMapper::toResponse).toList();
    }

    @Override
    public ProveedorResponseDTO getProveedorBySucursalAndActiveTrue(Long branchId, Long proveedorId) {
        Proveedor proveedor = proveedorSucursalRepository
                .findProveedorBySucursalAndProveedorId(branchId, proveedorId)
                .orElseThrow(() -> new NotFoundException("Proveedor no asociado a la sucursal del usuario loggeado, intenta de nuevo"));
        return proveedorMapper.toResponse(proveedor);
    }
}
