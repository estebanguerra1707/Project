package com.mx.mitienda.security;


import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.util.enums.Rol;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {
    private Usuario currentUser;

    public Usuario getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Usuario currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isSuperAdmin() {
        return currentUser != null && Rol.SUPER_ADMIN.equals(currentUser.getRole());
    }
}
