package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserServiceImpl implements IAuthenticatedUserService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return usuarioRepository.findByEmailAndActiveTrue(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado, favor de validar"));
    }

    @Override
    public Sucursal getCurrentBranch() {
        return getCurrentUser().getBranch();
    }

    @Override
    public Long getCurrentBranchId() {
        Sucursal branch = getCurrentBranch();
        if (branch == null) {
            throw new IllegalArgumentException("El usuario no tiene una sucursal asignada");
        }
        return branch.getId();
    }

    @Override
    public Long getCurrentBusinessTypeId() {
        Sucursal branch = getCurrentBranch();

        if (branch == null || branch.getBusinessType() == null) {
            throw new IllegalArgumentException("La sucursal no tiene un tipo de negocio asignado");
        }
        return branch.getBusinessType().getId();
    }

    @Override
    public Long getBusinessTypeIdFromSession() {
        Usuario usuario = getCurrentUser();

        return usuario.getBranch().getBusinessType().getId();
    }

    @Override
    public boolean isSuperAdmin() {
        Usuario u = getCurrentUser();
        return u != null && u.getRole() == Rol.SUPER_ADMIN;
    }

    @Override
    public boolean isAdmin() {
        Usuario u = getCurrentUser();
        return u != null && u.getRole() == Rol.ADMIN;
    }

    @Override
    public boolean isVendor() {
        Usuario u = getCurrentUser();
        return u != null && u.getRole() == Rol.VENDOR;
    }

    @Override
    public UserContext getUserContext() {

        Usuario user = getCurrentUser();
        boolean isSuper = user.getRole() == Rol.SUPER_ADMIN;
        boolean isAdmin = user.getRole() == Rol.ADMIN;

        Sucursal branch = null;
        Long branchId = null;
        Long businessTypeId = null;

        if (!isSuper) {
            branch = getCurrentBranch();
            branchId = branch.getId();
            businessTypeId = branch.getBusinessType().getId();
        }

        return new UserContext(
                isSuper,
                branchId,
                businessTypeId,
                user.getEmail(),
                isAdmin,
                branch
        );
    }

}
