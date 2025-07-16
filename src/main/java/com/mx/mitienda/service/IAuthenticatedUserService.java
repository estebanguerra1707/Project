package com.mx.mitienda.service;

import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;

public interface IAuthenticatedUserService {
    Usuario getCurrentUser();
    Sucursal getCurrentBranch();
    Long getCurrentBranchId();
    Long getCurrentBusinessTypeId();
    public Long getBusinessTypeIdFromSession();
}
