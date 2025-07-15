package com.mx.mitienda.service;

import com.mx.mitienda.model.InventarioSucursal;

import java.util.List;

public interface IGenerateEmailSimpleService {
    public String generarHtmlAlertaStock(InventarioSucursal inventarioSucursal);
    }
