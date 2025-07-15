package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAlertaInventarioService {
    public Page<InventarioAlertasDTO> obtenerProductosConStockCritico(Long sucursalId, Long categoriaId, Pageable pegable);
}
