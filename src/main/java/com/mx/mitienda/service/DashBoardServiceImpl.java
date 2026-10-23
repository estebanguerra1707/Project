package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.model.dto.DashboardResumenDTO;
import com.mx.mitienda.model.dto.ResumenSemanaDTO;
import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.model.dto.UsuarioVentaResumenDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
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
    private final GastoRepository gastoRepository;

    protected DashBoardServiceImpl(IAuthenticatedUserService authenticatedUserService, DetalleVentaRepository detalleVentaRepository,
                                   IAuthenticatedUserService authenticatedUserService1,
                                   VentaRepository ventaRepository, ProductoRepository productoRepository,
                                   InventarioSucursalRepository inventarioSucursalRepository,
                                   DashboardRepository dashboardRepository, GastoRepository gastoRepository) {
        super(authenticatedUserService);
        this.detalleVentaRepository = detalleVentaRepository;
        this.authenticatedUserService = authenticatedUserService1;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.dashboardRepository = dashboardRepository;
        this.gastoRepository = gastoRepository;
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
    public ResumenSemanaDTO obtenerResumenSemana(Long branchId, int semanasAtras) {
        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin() ? branchId : ctx.getBranchId();

        LocalDate today = LocalDate.now();
        LocalDateTime endToday = today.plusDays(1).atStartOfDay();

        LocalDate inicioSemana = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.THURSDAY))
                .minusWeeks(semanasAtras);

        LocalDateTime startSemana = inicioSemana.atStartOfDay();
        LocalDateTime endSemana = (semanasAtras == 0) ? endToday : inicioSemana.plusDays(7).atStartOfDay();

        BigDecimal vendidosSemana = ventaRepository.sumArticulosVendidos(startSemana, endSemana, sucursalId);
        if (vendidosSemana == null) vendidosSemana = BigDecimal.ZERO;

        List<com.mx.mitienda.model.dto.VentaDiariaDTO> ventasDiarias = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate diaIteracion = inicioSemana.plusDays(i);
            LocalDateTime startDia = diaIteracion.atStartOfDay();
            LocalDateTime endDia = diaIteracion.plusDays(1).atStartOfDay();

            BigDecimal ingresosDia = ventaRepository.sumVentasBrutas(startDia, endDia, sucursalId);
            BigDecimal gananciaDia = ventaRepository.sumGananciaVentas(startDia, endDia, sucursalId);
            BigDecimal vendidosDia = ventaRepository.sumArticulosVendidos(startDia, endDia, sucursalId);
            BigDecimal gastosDia = gastoRepository.sumGastosPorDiaYSucursal(diaIteracion, sucursalId);

            // Clasificación dinámica por Categoría (1 sola query a BD)
            List<Object[]> filasCostos = dashboardRepository.obtenerDesgloseCostosPorDia(startDia, endDia, sucursalId);

            BigDecimal cChilaquiles = BigDecimal.ZERO;
            BigDecimal cPanes = BigDecimal.ZERO;
            BigDecimal cOtros = BigDecimal.ZERO;

            BigDecimal pChilaquiles = BigDecimal.ZERO;
            BigDecimal pPanes = BigDecimal.ZERO;
            BigDecimal pOtros = BigDecimal.ZERO;

            for (Object[] fila : filasCostos) {
                String categoria = fila[0] != null ? ((String) fila[0]).toLowerCase().trim() : "";
                String producto = fila[1] != null ? ((String) fila[1]).toLowerCase().trim() : "";
                BigDecimal cantidad = fila[2] != null ? (BigDecimal) fila[2] : BigDecimal.ZERO;
                BigDecimal costo = fila[3] != null ? (BigDecimal) fila[3] : BigDecimal.ZERO;

                // 1. Chilaquiles (Cualquier variante de chilaquiles)
                if (categoria.contains("chilaquil") || producto.contains("chilaquil")) {
                    cChilaquiles = cChilaquiles.add(costo);
                    pChilaquiles = pChilaquiles.add(cantidad);
                }
                // 2. Panes / Postres (Categoría POSTRES, panadería, bolillos, etc.)
                else if (categoria.contains("postre") || categoria.contains("pan") || producto.contains("pan") || producto.contains("bolillo")) {
                    // Excepción por si hay un extra con pan (ej. Pan extra Oreo)
                    if (categoria.contains("extra") && !categoria.contains("postre")) {
                        cOtros = cOtros.add(costo);
                        pOtros = pOtros.add(cantidad);
                    } else {
                        cPanes = cPanes.add(costo);
                        pPanes = pPanes.add(cantidad);
                    }
                }
                // 3. Bebidas y Extras (Categoría "Bebidas", "Extras" o cualquier otra nueva)
                else if (categoria.contains("bebida") || categoria.contains("extra") || categoria.contains("cafe") || categoria.contains("coca")) {
                    cOtros = cOtros.add(costo);
                    pOtros = pOtros.add(cantidad);
                }
                // 4. Por si llega cualquier otra cosa sin clasificar, mandarla a extras para que no se pierda el costo
                else {
                    cOtros = cOtros.add(costo);
                    pOtros = pOtros.add(cantidad);
                }
            }

            BigDecimal ingresosSeguros = ingresosDia != null ? ingresosDia : BigDecimal.ZERO;
            BigDecimal gananciaBrutaSegura = gananciaDia != null ? gananciaDia : BigDecimal.ZERO;
            BigDecimal vendidosSeguros = vendidosDia != null ? vendidosDia : BigDecimal.ZERO;
            BigDecimal gastosSeguros = gastosDia != null ? gastosDia : BigDecimal.ZERO;

            BigDecimal gananciaNetaReal = gananciaBrutaSegura.subtract(gastosSeguros);

            String nombreDia = diaIteracion.getDayOfWeek().getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    new java.util.Locale("es", "MX")
            );
            nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1) + " " + diaIteracion.getDayOfMonth();

            ventasDiarias.add(new com.mx.mitienda.model.dto.VentaDiariaDTO(
                    nombreDia,
                    ingresosSeguros,
                    gananciaNetaReal,
                    vendidosSeguros,
                    gastosSeguros,
                    cChilaquiles,
                    cPanes,
                    cOtros,
                    pChilaquiles,
                    pPanes,
                    pOtros
            ));
        }
        return new ResumenSemanaDTO(ventasDiarias, vendidosSemana);
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

        LocalDateTime startSemana = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.THURSDAY)).atStartOfDay();
        BigDecimal vendidosHoy = ventaRepository.sumArticulosVendidos(startToday, endToday, sucursalId);
        BigDecimal vendidosSemana = ventaRepository.sumArticulosVendidos(startSemana, endToday, sucursalId);
        BigDecimal vendidosMes = ventaRepository.sumArticulosVendidos(startMonth, endMonth, sucursalId);

        dto.setProductosVendidosHoy(vendidosHoy);
        dto.setProductosVendidosSemana(vendidosSemana);
        dto.setProductosVendidosMes(vendidosMes);

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

        // 1. AJUSTE EN LA GANANCIA GLOBAL DE HOY
        BigDecimal ingresosHoy = ventaRepository.sumVentasBrutas(startToday, endToday, sucursalId);
        BigDecimal gananciaHoyBruta = ventaRepository.sumGananciaVentas(startToday, endToday, sucursalId);
        BigDecimal gastosHoy = gastoRepository.sumGastosPorDiaYSucursal(today, sucursalId); // NUEVO

        BigDecimal ingresosHoySeguros = ingresosHoy != null ? ingresosHoy : BigDecimal.ZERO;
        BigDecimal gananciaHoySegura = gananciaHoyBruta != null ? gananciaHoyBruta : BigDecimal.ZERO;
        BigDecimal gastosHoySeguros = gastosHoy != null ? gastosHoy : BigDecimal.ZERO;

        dto.setIngresosHoy(ingresosHoySeguros);
        // Restamos el gasto para que la tarjeta del dashboard muestre la ganancia neta
        dto.setGananciaHoy(gananciaHoySegura.subtract(gastosHoySeguros));

        // 2. AJUSTE EN EL BUCLE DE VENTAS DIARIAS
        java.time.LocalDate inicioSemana = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.THURSDAY));
        List<com.mx.mitienda.model.dto.VentaDiariaDTO> ventasDiarias = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate diaIteracion = inicioSemana.plusDays(i);
            LocalDateTime startDia = diaIteracion.atStartOfDay();
            LocalDateTime endDia = diaIteracion.plusDays(1).atStartOfDay();

            BigDecimal ingresosDia = ventaRepository.sumVentasBrutas(startDia, endDia, sucursalId);
            BigDecimal gananciaDia = ventaRepository.sumGananciaVentas(startDia, endDia, sucursalId);
            BigDecimal vendidosDia = ventaRepository.sumArticulosVendidos(startDia, endDia, sucursalId);
            BigDecimal gastosDia = gastoRepository.sumGastosPorDiaYSucursal(diaIteracion, sucursalId);

            List<Object[]> filasCostos = dashboardRepository.obtenerDesgloseCostosPorDia(startDia, endDia, sucursalId);

            BigDecimal cChilaquiles = BigDecimal.ZERO;
            BigDecimal cPanes = BigDecimal.ZERO;
            BigDecimal cOtros = BigDecimal.ZERO;

            BigDecimal pChilaquiles = BigDecimal.ZERO;
            BigDecimal pPanes = BigDecimal.ZERO;
            BigDecimal pOtros = BigDecimal.ZERO;

            for (Object[] fila : filasCostos) {
                String categoria = ((String) fila[0]).toLowerCase();
                String producto = ((String) fila[1]).toLowerCase();
                BigDecimal cantidad = (BigDecimal) fila[2];
                BigDecimal costo = (BigDecimal) fila[3];

                // 1. Chilaquiles (Categorías que contengan chilaquil)
                if (categoria.contains("chilaquil")) {
                    cChilaquiles = cChilaquiles.add(costo);
                    pChilaquiles = pChilaquiles.add(cantidad);
                }
                // 2. Panes / Postres (Categoría POSTRES o productos que empiezan/contienen pan, excepto los extras con pan)
                else if (categoria.contains("postre") || (producto.contains("pan") && !producto.contains("extra"))) {
                    cPanes = cPanes.add(costo);
                    pPanes = pPanes.add(cantidad);
                }
                // 3. Todo lo demás: Bebidas (Coca, Café) y Extras (Queso, Bistec, Pollo, Pan extra Oreo, etc.)
                else {
                    cOtros = cOtros.add(costo);
                    pOtros = pOtros.add(cantidad);
                }
            }

            BigDecimal ingresosSeguros = ingresosDia != null ? ingresosDia : BigDecimal.ZERO;
            BigDecimal gananciaBrutaSegura = gananciaDia != null ? gananciaDia : BigDecimal.ZERO;
            BigDecimal vendidosSeguros = vendidosDia != null ? vendidosDia : BigDecimal.ZERO;
            BigDecimal gastosSeguros = gastosDia != null ? gastosDia : BigDecimal.ZERO;

            BigDecimal gananciaNetaReal = gananciaBrutaSegura.subtract(gastosSeguros);

            String nombreDia = diaIteracion.getDayOfWeek().getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    new java.util.Locale("es", "MX")
            );
            nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1) + " " + diaIteracion.getDayOfMonth();

            ventasDiarias.add(new com.mx.mitienda.model.dto.VentaDiariaDTO(
                    nombreDia,
                    ingresosSeguros,
                    gananciaNetaReal,
                    vendidosSeguros,
                    gastosSeguros,
                    cChilaquiles,
                    cPanes,
                    cOtros,
                    pChilaquiles,
                    pPanes,
                    pOtros
            ));
        }

        dto.setVentasDiariasSemana(ventasDiarias);

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
