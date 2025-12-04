package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.DashboardResumenDTO;
import com.mx.mitienda.model.dto.TopProductoDTO;
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
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        long ventasHoy = ventaRepository.countByBranchIdAndSaleDateBetween(
                sucursalId,
                start,
                end
        );

        dto.setVentasHoy(ventasHoy);
        YearMonth ym = YearMonth.now();
        LocalDateTime startMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endMonth = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);

        BigDecimal ingresosMes = ventaRepository.sumGananciaNetaByMonth(
                sucursalId,
                startMonth,
                endMonth
        );
        dto.setIngresosMes(ingresosMes);
        return dto;
    }
    @Override
    public List<TopProductoDTO> topVendidos(LocalDate inicio, LocalDate fin, Long branchId) {

        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin() ? branchId : ctx.getBranchId();

        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.atTime(23, 59, 59, 999_999_999);
            // SUPER_ADMIN obtiene ambos gráficos
            return dashboardRepository.findTopProductosConsolidado(start, end, sucursalId);

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
