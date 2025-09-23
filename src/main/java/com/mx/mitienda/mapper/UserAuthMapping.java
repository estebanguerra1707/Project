package com.mx.mitienda.mapper;

import com.mx.mitienda.model.dto.RegisterRequestDTO;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.repository.SucursalRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserAuthMapping {

    @Autowired
    protected SucursalRepository sucursalRepository;

    public abstract Usuario toEntity(RegisterRequestDTO dto);

    @AfterMapping
    protected void setBranch(@MappingTarget Usuario u, RegisterRequestDTO dto) {
        Rol role = dto.getRole();
        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        if (role == Rol.SUPER_ADMIN) {
            u.setBranch(null);
            return;
        }
        switch (role) {
            case ADMIN:
            case VENDOR:
                if (dto.getBranchId() == null) {
                    throw new IllegalArgumentException("branchId es obligatorio para rol " + role);
                }
                Sucursal branch = sucursalRepository.findById(dto.getBranchId())
                        .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + dto.getBranchId()));
                u.setBranch(branch);
                break;
            default:
                u.setBranch(null);
        }
    }
}
