package com.mx.mitienda.service;


import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;

import java.util.List;
import java.util.Optional;

public interface IProveedorSucursalService {
        List<ProveedorResponseDTO> getProveedoresBySucursal(Long branchId);
        ProveedorResponseDTO getProveedorBySucursalAndActiveTrue(Long branchId, Long proveedorId);
}
