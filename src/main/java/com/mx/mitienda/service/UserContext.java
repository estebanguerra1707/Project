package com.mx.mitienda.service;

import com.mx.mitienda.model.Sucursal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserContext {
    private final boolean isSuperAdmin;
    private final Long branchId;
    private final Long businessTypeId;
    private final String email;
    private final boolean isAdmin;
    private final Sucursal branch;
}
