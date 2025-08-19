package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DevolucionComprasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DevolucionComprasReponseDTO;
import com.mx.mitienda.model.dto.DevolucionComprasRequestDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.util.Utils;
import com.mx.mitienda.util.enums.TipoDevolucion;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mx.mitienda.util.Utils.*;

@Service
@RequiredArgsConstructor
public class DevolucionComprasServiceImpl implements IDevolucionComprasService{

    private final ProductoRepository productoRepository;
    public final CompraRepository compraRepository;
    private final DetalleDevolucionComprasRepository detalleDevolucionComprasRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final HistorialMovimientoRepository historialMovimientoRepository;
    private final DevolucionComprasMapper devolucionComprasMapper;
    private final DevolucionComprasRepository devolucionComprasRepository;


    @Override
    @Transactional
    public DevolucionComprasReponseDTO procesarDevolucion(DevolucionComprasRequestDTO devolucionComprasRequestDTO, Authentication auth) {
        Usuario usuario = authenticatedUserService.getCurrentUser();
        Long branchId = authenticatedUserService.getCurrentBranchId();
        Long businessId = authenticatedUserService.getCurrentBusinessTypeId();

        // 1) Verificar la compra en la sucursal
        Compra compra = compraRepository.findByIdAndBranch_IdAndActiveTrue(devolucionComprasRequestDTO.getCompraId(), branchId)
                .orElseThrow(() -> new NotFoundException("Compra no encontrada en tu sucursal"));

        // 2) Buscar producto por código de barras (scoped al tipo de negocio)
        Producto producto = productoRepository.findByCodigoBarrasAndBusinessTypeId(devolucionComprasRequestDTO.getCodigoBarras(), businessId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado, validar: " + devolucionComprasRequestDTO.getCodigoBarras()));

        // 3) Match producto <-> detalle de la compra
        DetalleCompra detalleCompra = compra.getDetails().stream()
                .filter(d -> d.getProduct().getId().equals(producto.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("El producto no forma parte de la compra seleccionada"));

        // 4) Validar cantidades (no devolver más de lo comprado menos lo ya devuelto)
        int cantidadComprada = detalleCompra.getQuantity();
        int cantidadDevueltaAcumulada = Optional.ofNullable(
                detalleDevolucionComprasRepository.sumCantidadDevueltaPorCompraYProducto(compra.getId(), producto.getId())
        ).orElse(0);
        int cantidadRestante = cantidadComprada - cantidadDevueltaAcumulada;


        if (devolucionComprasRequestDTO.getCantidad() > cantidadRestante) {
            throw new IllegalArgumentException("No puedes devolver más de lo comprado");
        }

        // 5) Construir la devolución con MapStruct (cabecera + detalle)
        DevolucionCompras devolucion = devolucionComprasMapper.toEntity(
                devolucionComprasRequestDTO,
                compra,
                detalleCompra,
                usuario,
                authenticatedUserService.getCurrentBranch(),
                cantidadComprada
        );

        if (devolucion.getDetalles() != null) {
            devolucion.getDetalles().forEach(detalleDevolucionCompras -> detalleDevolucionCompras.setDevolucion(devolucion));
        }

        DevolucionCompras devolucionComprasGuardada = devolucionComprasRepository.save(devolucion);

        // 6) Actualizar inventario: devol. de compra => SALIDA (regresa al proveedor)
        InventarioSucursal inventario = inventarioSucursalRepository
                .findByProduct_IdAndBranch_Id(producto.getId(), branchId)
                .orElseThrow(() -> new NotFoundException("Inventario no encontrado"));
        int stockAnterior = inventario.getStock();
        int stockNuevo = stockAnterior - devolucionComprasRequestDTO.getCantidad();
        if (stockNuevo < 0) {
            throw new IllegalArgumentException("No hay stock suficiente para devolver al proveedor");
        }
        inventario.setStock(stockNuevo);
        inventarioSucursalRepository.save(inventario);

        // 7) Historial de movimientos: SALIDA
        HistorialMovimiento mov = new HistorialMovimiento();
        mov.setInventarioSucursal(inventario);
        mov.setQuantity(devolucionComprasRequestDTO.getCantidad());
        mov.setMovementType(TipoMovimiento.SALIDA);
        mov.setReference("Devolución de COMPRA #" + compra.getId());
        mov.setMovementDate(LocalDateTime.now());
        mov.setBeforeStock(stockAnterior);
        mov.setNewStock(stockNuevo);
        historialMovimientoRepository.save(mov);
        // 8) (Opcional) Ajustes contables: monto devuelto a proveedor, etc.
        // Si llevas un resumen, aquí puedes registrar el montoDevuelto para reportes.

        return devolucionComprasMapper.toResponse(devolucionComprasGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> contarPorDia(LocalDate desde, LocalDate hasta, Long branchId) {
        validarRango(desde, hasta);
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta);
        List<Object[]> conteoDevolucionesList = devolucionComprasRepository.countPorDia(branchId, inicio, fin);

        //se prellena un mapa ordenado y ligado que tenga las fechas desde hasta con valor en 0
        Map<LocalDate, Long> devolucionDiaMap = Utils.prellenarDiasConCero(desde, hasta);

        // se prellenan los datos de la BD
        for (Object[] conteoDevolucion : conteoDevolucionesList) {
            java.sql.Timestamp ts = (java.sql.Timestamp) conteoDevolucion[0];
            LocalDate periodo = ts.toLocalDateTime().toLocalDate(); // día (o lunes de la semana)
            long total = ((Number) conteoDevolucion[1]).longValue();
            devolucionDiaMap.put(periodo, total);
        }

        return devolucionDiaMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> contarPorSemana(LocalDate desde, LocalDate hasta, Long branchId) {
        validarRango(desde, hasta);
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta);
        List<Object[]> conteoDevolucionesList = devolucionComprasRepository.
                countPorSemana(branchId, inicio, fin);
        //se prellena un mapa ordenado y ligado que tenga las fechas desde hasta con valor en 0
        Map<LocalDate, Long> devolucionSemanaMap = Utils.prellenarSemanasConCero(desde, hasta);

        for (Object[] conteoDevolucion : conteoDevolucionesList) {
            LocalDate periodo = toLocalDate(conteoDevolucion[0]); // viene de date_trunc('week'|'day', ...)
            long total = ((Number) conteoDevolucion[1]).longValue();
            devolucionSemanaMap.put(periodo, total);
        }

        return devolucionSemanaMap;
    }


    @Override
    @Transactional(readOnly = true)
    public Map<YearMonth, Long> contarPorMes(LocalDate desde, LocalDate hasta, Long branchId) {
        validarRango(desde, hasta);
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta);

        List<Object[]> conteoDevolucionesList = devolucionComprasRepository.
                countPorMes(branchId, inicio, fin);
        //se prellena un mapa ordenado y ligado que tenga las fechas desde hasta con valor en 0
        Map<YearMonth, Long> devolucionMesMap = Utils.prellenarMesesConCero(desde, hasta);

        for (Object[] conteoDevolucion : conteoDevolucionesList) {
            YearMonth ym = toYearMonth(conteoDevolucion[0]);      // viene de date_trunc('month', ...)
            long total = ((Number) conteoDevolucion[1]).longValue();
            devolucionMesMap.put(ym, total);
        }
        return devolucionMesMap;
    }
    @Override
    public BigDecimal obtenerDevolucionesComprasPorRango(LocalDate desde, LocalDate hasta) {
        Long branchId = authenticatedUserService.getCurrentBranchId();
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta);
        return detalleDevolucionComprasRepository.sumMontoDevueltoCompras(branchId, inicio, fin);
    }

}
