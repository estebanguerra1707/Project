package com.mx.mitienda.service.base;

import com.mx.mitienda.service.AuthenticatedUserServiceImpl;
import com.mx.mitienda.service.IAuthenticatedUserService;
import com.mx.mitienda.service.UserContext;
import lombok.RequiredArgsConstructor;

public abstract  class BaseService {
    private final IAuthenticatedUserService authenticatedUserService;

    protected BaseService(IAuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

    protected UserContext ctx() {
        return authenticatedUserService.getUserContext();
    }

}
