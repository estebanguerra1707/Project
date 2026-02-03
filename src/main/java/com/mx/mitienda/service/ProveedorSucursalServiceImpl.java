package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProveedorMapper;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.ProveedorSucursal;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.repository.ProveedorSucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedorSucursalServiceImpl implements  IProveedorSucursalService{

    private final ProveedorSucursalRepository proveedorSucursalRepository;
    private final ProveedorMapper proveedorMapper;


    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> getProveedoresBySucursal(Long branchId) {

        List<Proveedor> proveedores =
                proveedorSucursalRepository.findProveedoresBySucursalId(branchId);

        if (proveedores.isEmpty()) {
            return List.of();
        }

        List<Long> proveedorIds = proveedores.stream()
                .map(Proveedor::getId)
                .toList();

        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorIdIn(proveedorIds);
        return proveedores.stream()
                .map(proveedor -> {
                    List<ProveedorSucursal> relsProveedor = relaciones.stream()
                            .filter(ps -> ps.getProveedor().getId().equals(proveedor.getId()))
                            .toList();

                    return proveedorMapper.toResponse(proveedor, relsProveedor);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO getProveedorBySucursalAndActiveTrue(
            Long branchId,
            Long proveedorId
    ) {
        Proveedor proveedor = proveedorSucursalRepository
                .findProveedorBySucursalAndProveedorId(branchId, proveedorId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Proveedor no asociado a la sucursal del usuario loggeado"
                        )
                );
        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorId(proveedor.getId());

        // 3️⃣ Mapear correctamente
        return proveedorMapper.toResponse(proveedor, relaciones);
    }
    
}
