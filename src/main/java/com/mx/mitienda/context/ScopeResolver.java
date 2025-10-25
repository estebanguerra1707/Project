package com.mx.mitienda.context;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.ProductoDTO;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.service.IAuthenticatedUserService;
import com.mx.mitienda.util.enums.Rol;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScopeResolver {

    private final IAuthenticatedUserService authenticatedUserService;
    private final SucursalRepository sucursalRepository;

    /**
     * Resuelve y valida el alcance (sucursal/tipoNegocio) para crear un producto.
     * - SUPER_ADMIN: sucursalId debe venir en el DTO y existir/estar activa.
     * - No SUPER_ADMIN: toma sucursal/tipoNegocio de la sesión; ignora lo que mande el DTO.
     */
    public Scope resolveForProductCreate(@NonNull ProductoDTO dto) {
        final boolean isSuper = authenticatedUserService.isSuperAdmin();
        final Long branchId;

        if (isSuper) {
            branchId = dto.getBranchId();
            if (branchId == null) {
                throw new IllegalArgumentException("Sucursal requerida para "+ Rol.SUPER_ADMIN);
            }
        } else {
            branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new IllegalArgumentException("El usuario" + authenticatedUserService.getCurrentUser().getUsername() +" no tiene una sucursal asignada");
            }
        }
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        // Tipo de negocio obligatorio
        if (sucursal.getBusinessType() == null) {
            throw new IllegalArgumentException("La sucursal" + sucursal.getName() +" no tiene un tipo de negocio asignado");
        }
        Long businessTypeId = sucursal.getBusinessType().getId();

        return new Scope(branchId, businessTypeId, sucursal);
    }
}
