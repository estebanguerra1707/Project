package com.mx.mitienda.repository;
import java.util.List;
import com.mx.mitienda.model.InventarioSucursal;

public interface InventarioAlertasRepository {
    List<InventarioSucursal> findByStockCriticoTrue();
}
