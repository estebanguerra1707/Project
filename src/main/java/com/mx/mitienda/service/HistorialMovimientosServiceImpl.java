package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.model.HistorialMovimiento;
import com.mx.mitienda.model.InventarioSucursal;

import com.mx.mitienda.model.dto.HistorialMovimientosResponseDTO;
import com.mx.mitienda.repository.HistorialMovimientoRepository;
import com.mx.mitienda.repository.InventarioSucursalRepository;

import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistorialMovimientosServiceImpl implements IHistorialMovimientosService {
    private final HistorialMovimientoRepository historialMovimientoRepository;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final InventarioSucursalRepository inventarioSucursalRepository;

    @Override
    public void registrarMovimiento(  InventarioSucursal inventarioSucursal,
                                      TipoMovimiento tipoMovimiento,
                                      Integer cantidad,
                                      Integer stockAnterior,
                                      Integer stockNuevo,
                                      String referencia){

        HistorialMovimiento historial = new HistorialMovimiento();
        historial.setInventarioSucursal(inventarioSucursal);
        historial.setMovementDate(LocalDateTime.now());
        historial.setMovementType(tipoMovimiento);
        historial.setQuantity(cantidad);
        historial.setBeforeStock(stockAnterior);
        historial.setNewStock(stockNuevo);
        historial.setReference(referencia);
        historialMovimientoRepository.save(historial);
    }

    public Page<HistorialMovimientosResponseDTO> obtenerPaginadoPorProducto(Long productoId, Pageable pageable) {
        if (authenticatedUserService.isSuperAdmin()) {
            return historialMovimientoRepository
                    .findByInventarioSucursal_Product_IdOrderByMovementDateDesc(productoId, pageable)
                    .map(this::toDTO);
        } else {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            return historialMovimientoRepository
                    .findByInventarioSucursal_Product_IdAndBranch_IdOrderByMovementDateDesc(productoId, branchId, pageable)
                    .map(this::toDTO);
        }
    }

    public Page<HistorialMovimientosResponseDTO> obtenerPaginadoPorInventario(Long inventarioId, Pageable pageable) {


        if (authenticatedUserService.isSuperAdmin()) {
            return historialMovimientoRepository
                    .findByInventarioSucursal_IdOrderByMovementDateDesc(inventarioId, pageable)
                    .map(this::toDTO);
        }
        // Validar que el inventario pertenece a la sucursal del usuario
        Long branchId = authenticatedUserService.getCurrentBranchId();
        boolean perteneceASucursal = inventarioSucursalRepository
                .existsByIdAndBranch_Id(inventarioId, branchId);

        if (!perteneceASucursal) {
            throw new ForbiddenException("No tienes permisos para acceder a este inventario.");
        }

        return historialMovimientoRepository
                .findByInventarioSucursal_IdAndBranch_IdOrderByMovementDateDesc(inventarioId, branchId, pageable)
                .map(this::toDTO);
    }

    private HistorialMovimientosResponseDTO toDTO(HistorialMovimiento historialMovimiento) {
        HistorialMovimientosResponseDTO dto = new HistorialMovimientosResponseDTO();
        dto.setMovementDate(historialMovimiento.getMovementDate());
        dto.setMovementType(historialMovimiento.getMovementType());
        dto.setQuantity(historialMovimiento.getQuantity());
        dto.setBeforeStock(historialMovimiento.getBeforeStock());
        dto.setNewStock(historialMovimiento.getNewStock());
        dto.setReference(historialMovimiento.getReference());
        return dto;
    }


}
