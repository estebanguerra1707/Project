package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.repository.UsuarioRepository;
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

        return usuarioRepository.findByUsername(username).orElseThrow(()-> new NotFoundException("Usuario no encontrado"));
    }

    @Override
    public Sucursal getCurrentBranch() {
        return getCurrentUser().getBranch();
    }

    @Override
    public Long getCurrentBranchId() {
        Sucursal branch = getCurrentBranch();
        if(branch == null) throw new IllegalArgumentException("El usuario no tiene una sucirdal asignada");
        return branch.getId();
    }

    @Override
    public Long getCurrentBusinessTypeId() {
        Sucursal branch = getCurrentBranch();
        if(branch == null || getCurrentBranch().getBusinessType()== null){
            throw new IllegalArgumentException("La sucursal no tiene un tipo de negocio asignado");
        }
        return branch.getBusinessType().getId();
    }
}
