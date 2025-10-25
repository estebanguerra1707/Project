package com.mx.mitienda.context;

import com.mx.mitienda.model.Sucursal;

public record Scope(Long branchId, Long businessTypeId, Sucursal sucursal) {}

