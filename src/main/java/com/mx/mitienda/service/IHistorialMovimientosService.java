package com.mx.mitienda.service;

import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.HistorialMovimientosResponseDTO;
import com.mx.mitienda.util.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IHistorialMovimientosService {
    public void registrarMovimiento(
            InventarioSucursal inventarioSucursal,
            TipoMovimiento tipoMovimiento,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockNuevo,
            String referencia);

    public Page<HistorialMovimientosResponseDTO> obtenerPaginadoPorProducto(Long productoId, Pageable pageable) ;
    public Page<HistorialMovimientosResponseDTO> obtenerPaginadoPorInventario(Long inventarioId, Pageable pageable);
}
