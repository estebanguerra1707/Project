package com.mx.mitienda.service;

import com.mx.mitienda.model.InventarioSucursal;

import java.util.List;

public interface IAlertaCorreoService {
    public void notificarStockCritico(InventarioSucursal inventario);
}
