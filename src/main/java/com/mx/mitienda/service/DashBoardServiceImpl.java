package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.DashboardResumenDTO;
import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.model.dto.UsuarioVentaResumenDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DashBoardServiceImpl extends BaseService implements IDashboardService{

    private final DetalleVentaRepository detalleVentaRepository;
    private final IAuthenticatedUserService authenticatedUserService;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final DashboardRepository dashboardRepository;

    protected DashBoardServiceImpl(IAuthenticatedUserService authenticatedUserService, DetalleVentaRepository detalleVentaRepository,
                                   IAuthenticatedUserService authenticatedUserService1,
                                   VentaRepository ventaRepository, ProductoRepository productoRepository,
                                   InventarioSucursalRepository inventarioSucursalRepository,
                                   DashboardRepository dashboardRepository) {
        super(authenticatedUserService);
        this.detalleVentaRepository = detalleVentaRepository;
        this.authenticatedUserService = authenticatedUserService1;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public List<TopProductoDTO> getTopProductos(String groupBy, LocalDateTime start, LocalDateTime end, Long branchId) {

        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin()
                ? branchId
                : ctx.getBranchId();


        if (ctx.isSuperAdmin()) {
            // SUPER_ADMIN ve todo
            return switch (groupBy.toLowerCase()) {
                case "day", "dia" -> detalleVentaRepository.findTopProductosPorDia(start, end);
                case "week", "semana" -> detalleVentaRepository.findTopProductosPorSemana(start, end);
                case "month", "mes" -> detalleVentaRepository.findTopProductosPorMes(start, end);
                default -> throw new IllegalArgumentException("groupBy inválido");
            };
        }
        if (sucursalId == null) {
            throw new ForbiddenException("Tu usuario no tiene una sucursal asignada.");
        }

        return switch (groupBy.toLowerCase()) {
            case "day", "dia" -> detalleVentaRepository.findTopProductosPorDiaAndSucursal(start, end, sucursalId);
            case "week", "semana" -> detalleVentaRepository.findTopProductosPorSemanaAndSucursal(start, end, sucursalId);
            case "month", "mes" -> detalleVentaRepository.findTopProductosPorMesAndSucursal(start, end, sucursalId);
            default -> throw new IllegalArgumentException("groupBy inválido");
        };

    }
    @Override
    public DashboardResumenDTO obtenerResumen(Long branchId) {
        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin()
                ? branchId
                : ctx.getBranchId();

        DashboardResumenDTO dto = new DashboardResumenDTO();

        dto.setTotalProductos(productoRepository.countByBranchId(sucursalId));
        dto.setProductosCriticos(inventarioSucursalRepository.countByBranchIdAndStockCriticoTrue(sucursalId));

        LocalDate today = LocalDate.now();
        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.plusDays(1).atStartOfDay();

        long ventasHoy = ventaRepository.countByBranchIdAndSaleDateBetween(
                sucursalId,
                startToday,
                endToday
        );

        dto.setVentasHoy(ventasHoy);

        YearMonth ym = YearMonth.now();
        LocalDateTime startMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endMonth = ym.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal ingresosMes = ventaRepository.sumVentasBrutas(
                startMonth,
                endMonth,
                sucursalId
        );

        dto.setIngresosMes(ingresosMes);

        dto.setVentasHoyPorUsuario(
                dashboardRepository.findVentasResumenPorUsuario(
                        startToday,
                        endToday,
                        sucursalId
                )
        );

        dto.setIngresosMesPorUsuario(
                dashboardRepository.findVentasResumenPorUsuario(
                        startMonth,
                        endMonth,
                        sucursalId
                )
        );

        return dto;
    }

    @Override
    public List<TopProductoDTO> topVendidos(LocalDate inicio, LocalDate fin, Long branchId) {
        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin() ? branchId : ctx.getBranchId();

        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.atTime(23, 59, 59, 999_999_999);

        List<TopProductoDTO> filas =
                dashboardRepository.findTopProductosConUsuariosDetalle(start, end, sucursalId);

        return agruparProductosConUsuarios(filas);
    }

    private List<TopProductoDTO> agruparProductosConUsuarios(List<TopProductoDTO> filas) {
        Map<String, TopProductoDTO> map = new LinkedHashMap<>();

        for (TopProductoDTO fila : filas) {
            String key = fila.getProductName()
                    + "|"
                    + Objects.toString(fila.getCategoria(), "")
                    + "|"
                    + Objects.toString(fila.getTipoNegocio(), "");

            TopProductoDTO acumulado = map.computeIfAbsent(key, k -> {
                TopProductoDTO dto = new TopProductoDTO();
                dto.setProductName(fila.getProductName());
                dto.setCategoria(fila.getCategoria());
                dto.setTipoNegocio(fila.getTipoNegocio());
                dto.setBranchName(fila.getBranchName());
                dto.setUltimaVenta(fila.getUltimaVenta());
                dto.setTotalQuantity(BigDecimal.ZERO);
                dto.setTotalIncome(BigDecimal.ZERO);
                dto.setNetProfit(BigDecimal.ZERO);
                dto.setUsuarios(new ArrayList<>());
                return dto;
            });

            BigDecimal cantidadFila = fila.getTotalQuantity() != null
                    ? fila.getTotalQuantity()
                    : BigDecimal.ZERO;

            BigDecimal ingresoFila = fila.getTotalIncome() != null
                    ? fila.getTotalIncome()
                    : BigDecimal.ZERO;

            BigDecimal gananciaFila = fila.getNetProfit() != null
                    ? fila.getNetProfit()
                    : BigDecimal.ZERO;

            acumulado.setTotalQuantity(
                    acumulado.getTotalQuantity().add(cantidadFila)
            );

            acumulado.setTotalIncome(
                    acumulado.getTotalIncome().add(ingresoFila)
            );

            acumulado.setNetProfit(
                    acumulado.getNetProfit().add(gananciaFila)
            );

            if (fila.getUltimaVenta() != null &&
                    (acumulado.getUltimaVenta() == null ||
                            fila.getUltimaVenta().isAfter(acumulado.getUltimaVenta()))) {
                acumulado.setUltimaVenta(fila.getUltimaVenta());
            }

            UsuarioVentaResumenDTO usuarioResumen = new UsuarioVentaResumenDTO();
            usuarioResumen.setUserId(fila.getUserId());
            usuarioResumen.setUsername(fila.getUsername());
            usuarioResumen.setTotalQuantity(cantidadFila);
            usuarioResumen.setTotalIncome(ingresoFila);
            usuarioResumen.setNetProfit(gananciaFila);
            usuarioResumen.setSalesCount(fila.getSalesCount() != null ? fila.getSalesCount() : 0L);

            acumulado.getUsuarios().add(usuarioResumen);
        }

        return map.values()
                .stream()
                .sorted((a, b) -> b.getTotalQuantity().compareTo(a.getTotalQuantity()))
                .toList();
    }

    // Nuevo método exclusivo para super user
    @Override
    public List<TopProductoDTO> topVendidosPorUsuario(LocalDate inicio, LocalDate fin, Long branchId) {

        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin() ? branchId : ctx.getBranchId();

        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.atTime(23, 59, 59, 999_999_999);

        return dashboardRepository.findTopProductosPorUsuario(start, end, sucursalId);
    }
}
