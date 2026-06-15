package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.exception.PdfGenerationException;
import com.mx.mitienda.mapper.DetalleVentaMapper;
import com.mx.mitienda.mapper.VentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.NumberToWordsConverter;
import com.mx.mitienda.util.VentaSpecBuilder;
import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.Utils.*;
import org.springframework.data.domain.PageImpl;

@Slf4j
@Service
public class VentaServiceImpl extends BaseService implements IVentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VentasMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final TemplateEngine templateEngine;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final IHistorialMovimientosService historialMovimientosService;
    private final IAlertaCorreoService alertaCorreoService;
    private final IGeneratePdfService generatePdfService;
    private final MailService mailService;
    private final DevolucionVentasRepository devolucionVentasRepository;
    private final DetalleDevolucionVentasRepository detalleDevolucionVentasRepository;

    public VentaServiceImpl(
            IAuthenticatedUserService authenticatedUserService,
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            VentasMapper ventaMapper,
            DetalleVentaMapper detalleVentaMapper,
            TemplateEngine templateEngine,
            InventarioSucursalRepository inventarioSucursalRepository,
            IHistorialMovimientosService historialMovimientosService,
            IAlertaCorreoService alertaCorreoService,
            IGeneratePdfService generatePdfService,
            MailService mailService,
            DevolucionVentasRepository devolucionVentasRepository,
            DetalleDevolucionVentasRepository detalleDevolucionVentasRepository
    ) {
        super(authenticatedUserService);
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.ventaMapper = ventaMapper;
        this.detalleVentaMapper = detalleVentaMapper;
        this.templateEngine = templateEngine;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.historialMovimientosService = historialMovimientosService;
        this.alertaCorreoService = alertaCorreoService;
        this.generatePdfService = generatePdfService;
        this.mailService = mailService;
        this.devolucionVentasRepository = devolucionVentasRepository;
        this.detalleDevolucionVentasRepository = detalleDevolucionVentasRepository;
    }
    private BigDecimal safe(BigDecimal venta) {
        return venta != null ? venta : BigDecimal.ZERO;
    }
    private record Totales(BigDecimal brutas, BigDecimal netas, BigDecimal gananciaNeta) {}

    @Override
    @Transactional
    public VentaResponseDTO registerSell(VentaRequestDTO request) {
        Venta venta = ventaMapper.toEntity(request, ctx().getEmail());

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : ventaGuardada.getDetailsList()) {

            Sucursal sucursal = ventaGuardada.getBranch();

            boolean isInventarioPorduenio =
                    Boolean.TRUE.equals(sucursal.getUsaInventarioPorDuenio());

            InventarioSucursal inventarioSucursal;

            if (isInventarioPorduenio) {
                if (detalle.getOwnerType() == null) {
                    throw new IllegalArgumentException(
                            "Debe seleccionar si el producto " + detalle.getProduct().getName()+ " es PROPIO o de CONSIGNACIÓN"
                    );
                }
                InventarioOwnerType ownerType = detalle.getOwnerType();

                Optional<InventarioSucursal> inventarioOpt =
                        inventarioSucursalRepository
                                .findByProduct_IdAndBranch_IdAndOwnerType(
                                        detalle.getProduct().getId(),
                                        sucursal.getId(),
                                        ownerType
                                );
                if(inventarioOpt.isPresent()){
                    inventarioSucursal = inventarioOpt.get();
                }else{
                    InventarioOwnerType otroOwnerType =
                            ownerType == InventarioOwnerType.PROPIO
                                    ? InventarioOwnerType.CONSIGNACION
                                    : InventarioOwnerType.PROPIO;

                    throw new IllegalArgumentException(
                            "No existe el producto "
                                    + detalle.getProduct().getName()
                                    + " en el inventario, intenta colocar TIPO DE DUEÑO: " +otroOwnerType
                    );
                }
            } else {
                List<InventarioSucursal> inventarios =
                        inventarioSucursalRepository.findByProduct_IdAndBranch_Id(
                                detalle.getProduct().getId(),
                                sucursal.getId()
                        );

                if (inventarios.isEmpty()) {
                    throw new IllegalArgumentException(
                            "No hay inventario para el producto "
                                    + detalle.getProduct().getName()
                    );
                }

                inventarioSucursal = inventarios.get(0);
            }

            BigDecimal stockAnterior = Optional.ofNullable(inventarioSucursal.getStock()).orElse(BigDecimal.ZERO);
            BigDecimal cantidad = Optional.ofNullable(detalle.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal stockNuevo = stockAnterior.subtract(cantidad);


            if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "No hay suficiente stock para vender: "
                                + detalle.getProduct().getName()
                );
            }

            inventarioSucursal.setStock(stockNuevo);
            if (inventarioSucursal.getMinStock() != null) {
                inventarioSucursal.setStockCritico(
                        stockNuevo.compareTo(inventarioSucursal.getMinStock()) <= 0
                );
            }else {
                inventarioSucursal.setStockCritico(false);
            }

            inventarioSucursalRepository.save(inventarioSucursal);

            if (Boolean.TRUE.equals(inventarioSucursal.getStockCritico())
                    && Boolean.TRUE.equals(sucursal.getAlertaStockCritico())) {
                alertaCorreoService.notificarStockCritico(inventarioSucursal);
            }

            historialMovimientosService.registrarMovimiento(
                    inventarioSucursal,
                    TipoMovimiento.SALIDA,
                    detalle.getQuantity(),
                    stockAnterior,
                    stockNuevo,
                    "Venta #" + ventaGuardada.getId()
            );
        }
      //envio venta por email
        generateSaleOutput(ventaGuardada, request.getIsPrinted(), request.getEmailList());
        return ventaMapper.toResponse(ventaGuardada);
    }
    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> getAll() {
        UserContext ctx = ctx();

        List<Venta> ventas = ctx.isSuperAdmin()
                ? ventaRepository.findAllActiveHeader()
                : ventaRepository.findAllActiveHeaderByBranch(ctx.getBranchId());

        return construirFilasTablaVentas(ventas);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, int page, int size) {
        UserContext ctx = ctx();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean isSuper = ctx.isSuperAdmin();
        boolean isAdmin = ctx.isAdmin();
        boolean isVendor = !isSuper && !isAdmin;

        Boolean consolidatedFilter = filterDTO.getConsolidated();

        VentaSpecBuilder builder = new VentaSpecBuilder()
                .client(filterDTO.getClienteId())
                .dateBetween(filterDTO.getStartDate(), filterDTO.getEndDate())
                .totalMajorTo(filterDTO.getMin())
                .totalMinorTo(filterDTO.getMax())
                .exactTotal(filterDTO.getTotalAmount())
                .active(true)
                .consolidated(consolidatedFilter)
                .sellPerDayMonthYear(filterDTO.getDay(), filterDTO.getMonth(), filterDTO.getYear())
                .sellPerMonthYear(filterDTO.getMonth(), filterDTO.getYear())
                .byPaymentMethod(filterDTO.getPaymentMethodId())
                .withId(filterDTO.getId());
        if (isSuper) {
            builder.username(filterDTO.getUsername());
            builder.userId(filterDTO.getUserId());
        } else if (isAdmin) {
            builder.userId(filterDTO.getUserId());
        } else if (isVendor) {
            builder.username(ctx.getEmail());
        }
        Specification<Venta> spec = builder.build();

        if (!isSuper) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("branch").get("id"), ctx.getBranchId()));
        }

        List<Venta> ventasFiltradas = ventaRepository.findAll(
                spec,
                Sort.by("id").descending()
        );

        List<VentaResponseDTO> filas = construirFilasTablaVentas(ventasFiltradas);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filas.size());

        List<VentaResponseDTO> pageContent = start >= filas.size()
                ? List.of()
                : filas.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filas.size());
    }
    @Override
    @Transactional(readOnly = true)
    public List<DetalleVentaResponseDTO> getDetailsPerSale(Long idVenta) {
        UserContext ctx = ctx();

        List<DetalleVenta> detalles = ctx.isSuperAdmin()
                ? detalleVentaRepository.findFullByVentaId(idVenta)
                : detalleVentaRepository.findFullByVentaIdAndBranchId(idVenta, ctx.getBranchId());

        return detalles.stream()
                .map(detalleVentaMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO getById(Long id) {
        Venta venta = ventaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        return ventaMapper.toResponse(venta);
    }
    @Override
    @Transactional
    public void deleteById(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        venta.setActive(false);
        ventaRepository.save(venta);
    }
    @Override
    @Transactional(readOnly = true)
    public byte[] generateTicketPdf(Long idVenta) {
        Venta venta = ventaRepository.findByIdFull(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        Context context = new Context();
        context.setVariable("venta", venta);
        context.setVariable("branch", venta.getBranch());
        context.setVariable("fechaFormateada",
                venta.getSaleDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );

        String html = templateEngine.process("ticket_venta", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new PdfGenerationException("Error generando PDF de la venta", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findCurrentUserVentas() {
        UserContext ctx = ctx();
        return ventaRepository.findByBranchAndBusinessType(ctx.getBranchId(), ctx.getBusinessTypeId())
                .stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    // cálculo central [inicio, fin)
    private Totales calcularTotales(LocalDateTime inicio, LocalDateTime fin, Long branchId) {
        UserContext ctx = ctx();
        Long sucursalId = ctx.isSuperAdmin()
                ? branchId
                : ctx.getBranchId();


        BigDecimal ventasBrutas  = safe(ventaRepository.sumVentasBrutas(inicio, fin, sucursalId));
        BigDecimal devoluciones    = safe(devolucionVentasRepository.sumImporteDevuelto(inicio, fin, sucursalId));
        BigDecimal ventasNetas   = ventasBrutas.subtract(devoluciones);

        BigDecimal gananciaVentas = safe(ventaRepository.sumGananciaVentas(inicio, fin, sucursalId));
        BigDecimal ganaciaDevoluciones    = safe(devolucionVentasRepository.sumGananciaPerdidaPorDevoluciones(inicio, fin, sucursalId));
        BigDecimal gananciaNeta   = gananciaVentas.subtract(ganaciaDevoluciones);

        return new Totales(ventasBrutas, ventasNetas, gananciaNeta);
    }

    @Override
    public BigDecimal obtenerGananciaHoy(Long branchId) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59, 999_000_000); // 23:59:59.999
        return calcularTotales(inicio, fin, branchId).gananciaNeta();
    }

    @Override
    public BigDecimal obtenerGananciaSemana(Long branchId) {

        LocalDateTime inicio = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime fin = LocalDate.now()
                .with(DayOfWeek.SUNDAY)
                .atTime(23, 59, 59, 999_000_000);
        return calcularTotales(inicio, fin, branchId).gananciaNeta();
    }

    @Override
    public BigDecimal obtenerGananciaMes(Long branchId) {

        LocalDateTime inicio = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
        LocalDateTime fin = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(23, 59, 59, 999_000_000);
        return calcularTotales(inicio, fin, branchId).gananciaNeta();
    }

    @Override
    public BigDecimal obtenerGananciaPorDia(LocalDate dia, Long branchId) {

        LocalDateTime inicio = inicioDelDia(dia);
        LocalDateTime fin = dia.plusDays(1).atStartOfDay();
        return calcularTotales(inicio, fin, branchId).gananciaNeta();
    }


    @Override
    public BigDecimal obtenerGananciaPorRango(GananciaPorFechaDTO gananciaPorFechaDTO) {
        LocalDateTime inicio = inicioDelDia(gananciaPorFechaDTO.getStartDate());
        LocalDateTime fin = finDelDia(gananciaPorFechaDTO.getEndDate());
        return calcularTotales(inicio, fin, gananciaPorFechaDTO.getBranchId()).gananciaNeta();
    }

    @Override
    public Map<LocalDate, BigDecimal> obtenerGananciasPorDiaEnRango(GananciaPorFechaDTO gananciaPorFechaDTO) {
        if (gananciaPorFechaDTO.getStartDate().isAfter(gananciaPorFechaDTO.getEndDate())) {
            throw new IllegalArgumentException("Rango de fechas mal asignado");
        }

        Map<LocalDate, BigDecimal> resultado = new LinkedHashMap<>();
        LocalDate diaActual = gananciaPorFechaDTO.getStartDate();

        while (!diaActual.isAfter(gananciaPorFechaDTO.getEndDate())) {
            LocalDateTime inicio = inicioDelDia(diaActual);
            LocalDateTime fin    = finDelDia(diaActual);
            BigDecimal ganancia = calcularTotales(inicio, fin, gananciaPorFechaDTO.getBranchId()).gananciaNeta();
            resultado.put(diaActual, ganancia != null ? ganancia : BigDecimal.ZERO);
            diaActual = diaActual.plusDays(1);
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerGananciaPorVenta(Long ventaId, Long branchId) {
        UserContext ctx = ctx();

        Long sucursalId = ctx.isSuperAdmin()
                ? branchId
                : ctx.getBranchId();

        Venta venta = ventaRepository.findByIdWithDetails(ventaId, sucursalId)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada o no corresponde a tu sucursal"));

        return venta.getDetailsList()
                .stream()
                .map(detalleVenta -> {

                    BigDecimal precioVenta = Optional.ofNullable(detalleVenta.getUnitPrice()).orElse(BigDecimal.ZERO);
                    BigDecimal precioCompra = Optional.ofNullable(detalleVenta.getProduct().getPurchasePrice()).orElse(BigDecimal.ZERO);

                    BigDecimal cantidadVendida = Optional.ofNullable(detalleVenta.getQuantity()).orElse(BigDecimal.ZERO);

                    // Cantidad devuelta de este detalle
                    BigDecimal cantidadDevuelta = Optional.ofNullable(
                            detalleDevolucionVentasRepository.sumCantidadDevuelta(detalleVenta.getId())
                    ).orElse(BigDecimal.ZERO);

                    // Cantidad efectiva (vendida - devuelta)
                    BigDecimal cantidadEfectiva = cantidadVendida.subtract(cantidadDevuelta);
                    if (cantidadEfectiva.compareTo(BigDecimal.ZERO) < 0) {
                        cantidadEfectiva = BigDecimal.ZERO;
                    }
                    BigDecimal margen = precioVenta.subtract(precioCompra);
                    return margen.multiply(cantidadEfectiva);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override
    public BigDecimal obtenerVentasBrutasPorRango(GananciaPorFechaDTO gananciaPorFechaDTO) {
        LocalDateTime inicio = inicioDelDia(gananciaPorFechaDTO.getStartDate());
        LocalDateTime fin = finDelDia(gananciaPorFechaDTO.getEndDate()); // hasta las 23:59:59.999
        return calcularTotales(inicio, fin, gananciaPorFechaDTO.getBranchId()).brutas();
    }
    @Override
    public BigDecimal obtenerVentasNetasPorRango(GananciaPorFechaDTO gananciaPorFechaDTO) {
        LocalDateTime inicio = inicioDelDia(gananciaPorFechaDTO.getStartDate());
        LocalDateTime fin = finDelDia(gananciaPorFechaDTO.getEndDate());
        return calcularTotales(inicio, fin, gananciaPorFechaDTO.getBranchId()).netas();
    }


    private List<VentaResponseDTO> construirFilasTablaVentas(List<Venta> ventas) {
        List<VentaResponseDTO> resultado = new ArrayList<>();

        List<Venta> ventasNormales = ventas.stream()
                .filter(v -> !Boolean.TRUE.equals(v.getConsolidated()))
                .toList();

        Map<Long, List<Venta>> ventasConsolidadasPorTicket = ventas.stream()
                .filter(v -> Boolean.TRUE.equals(v.getConsolidated()))
                .filter(v -> v.getWeeklyTicketId() != null)
                .collect(Collectors.groupingBy(
                        Venta::getWeeklyTicketId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        ventasNormales.stream()
                .map(ventaMapper::toResponseHeader)
                .forEach(resultado::add);

        ventasConsolidadasPorTicket.forEach((weeklyTicketId, ventasDelTicket) -> {
            VentaResponseDTO filaConsolidada =
                    ventaMapper.toConsolidadaVirtualHeader(weeklyTicketId, ventasDelTicket);

            if (filaConsolidada != null) {
                resultado.add(filaConsolidada);
            }
        });

        resultado.sort(
                Comparator.comparing(
                        VentaResponseDTO::getSaleDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return resultado;
    }
    private void generateSaleOutput(Venta venta, Boolean printed, List<String> emailList) {
        byte[] pdfBytes = generatePdfService.generatePdf(VENTA_CODE, venta.getId(), printed != null && printed);

        if (emailList != null && !emailList.isEmpty()) {
            mailService.sendPDFEmail(
                    emailList,
                    "Venta",
                    "Comprobante de " + VENTA_CODE,
                    "<p>Adjunto encontrarás tu comprobante de venta.</p>",
                    pdfBytes,
                    VENTA_CODE+"_" + venta.getId() + ".pdf"
            );
        }

        if (printed != null && printed) {
            log.info("Ticket térmico generado para venta {}", venta.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VentaConsolidadaResponseDTO generarDetalleConsolidado(VentaConsolidadaRequestDTO request) {
        List<Venta> ventas = buscarYValidarVentasParaConsolidar(request);
        return construirDetalleConsolidado(request, ventas);
    }

    @Override
    @Transactional
    public VentaConsolidadaResponseDTO generarVentaConsolidada(VentaConsolidadaRequestDTO request) {
        List<Venta> ventas = buscarYValidarVentasParaConsolidar(request);

        Long weeklyTicketId = System.currentTimeMillis();
        LocalDateTime consolidatedAt = LocalDateTime.now();

        ventas.forEach(venta -> {
            venta.setConsolidated(true);
            venta.setWeeklyTicketId(weeklyTicketId);
            venta.setConsolidatedAt(consolidatedAt);
        });

        ventaRepository.saveAll(ventas);

        VentaConsolidadaResponseDTO detalle = construirDetalleConsolidado(request, ventas);

        detalle.setWeeklyTicketId(weeklyTicketId);
        detalle.setConsolidatedAt(consolidatedAt);

        return detalle;
    }

    @Override
    @Transactional
    public byte[] generarTicketConsolidadoPdf(VentaConsolidadaRequestDTO request) {
        List<Venta> ventas = buscarYValidarVentasParaConsolidar(request);
        VentaConsolidadaResponseDTO detalle = construirDetalleConsolidado(request, ventas);
        Sucursal branch = ventas.get(0).getBranch();

        byte[] pdf = generatePdfService.generateVentaConsolidadaPdf(detalle, branch, false);
        Long weeklyTicketId = System.currentTimeMillis();
        LocalDateTime consolidatedAt = LocalDateTime.now();

        ventas.forEach(venta -> {
            venta.setConsolidated(true);
            venta.setWeeklyTicketId(weeklyTicketId);
            venta.setConsolidatedAt(consolidatedAt);
        });

        ventaRepository.saveAll(ventas);

        return pdf;
    }
    private List<Venta> buscarYValidarVentasParaConsolidar(VentaConsolidadaRequestDTO request) {
        if (request.getVentaIds() == null || request.getVentaIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una venta");
        }

        validarPeriodoMartesALunes(request.getStartDate(), request.getEndDate());

        UserContext ctx = ctx();
        Long branchId = ctx.isSuperAdmin() ? null : ctx.getBranchId();

        List<Venta> ventas = ventaRepository.findSelectedForConsolidation(
                request.getVentaIds(),
                branchId
        );

        if (ventas.size() != request.getVentaIds().size()) {
            throw new IllegalArgumentException(
                    "Una o más ventas no existen, ya están consolidadas o no pertenecen a tu sucursal"
            );
        }
        Long clienteBaseId = ventas.get(0).getClient().getId();

        Long vendedorBaseId = ventas.get(0).getUsuario() != null
                ? ventas.get(0).getUsuario().getId()
                : null;

        if (vendedorBaseId == null) {
            throw new IllegalArgumentException("No se pudo determinar el vendedor de la venta base");
        }

        for (Venta venta : ventas) {
            if (!Objects.equals(venta.getClient().getId(), clienteBaseId)) {
                throw new IllegalArgumentException("Todas las ventas seleccionadas deben pertenecer al mismo cliente");
            }

            Long vendedorVentaId = venta.getUsuario() != null
                    ? venta.getUsuario().getId()
                    : null;

            if (!Objects.equals(vendedorVentaId, vendedorBaseId)) {
                throw new IllegalArgumentException("Todas las ventas seleccionadas deben pertenecer al mismo vendedor");
            }

            if (request.getClienteId() != null &&
                    !Objects.equals(venta.getClient().getId(), request.getClienteId())) {
                throw new IllegalArgumentException("Las ventas seleccionadas no pertenecen al cliente filtrado");
            }

            if (request.getUserId() != null &&
                    !Objects.equals(vendedorVentaId, request.getUserId())) {
                throw new IllegalArgumentException("Todas las ventas seleccionadas deben pertenecer al vendedor seleccionado");
            }

            if (venta.getSaleDate() == null ||
                    venta.getSaleDate().isBefore(request.getStartDate()) ||
                    venta.getSaleDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Todas las ventas seleccionadas deben estar dentro del periodo martes a lunes");
            }

            if (Boolean.TRUE.equals(venta.getConsolidated())) {
                throw new IllegalArgumentException("Una o más ventas ya fueron consolidadas");
            }
        }

        return ventas;
    }

    private void validarPeriodoMartesALunes(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("El rango de fechas es obligatorio");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser mayor a la fecha final");
        }

        LocalDate inicio = startDate.toLocalDate();
        LocalDate fin = endDate.toLocalDate();

        if (inicio.getDayOfWeek() != DayOfWeek.TUESDAY) {
            throw new IllegalArgumentException("El periodo debe iniciar en martes");
        }

        if (fin.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("El periodo debe terminar en lunes");
        }

        if (!fin.equals(inicio.plusDays(6))) {
            throw new IllegalArgumentException("El periodo debe ser exactamente de martes a lunes");
        }
    }

    private VentaConsolidadaResponseDTO construirDetalleConsolidado(
            VentaConsolidadaRequestDTO request,
            List<Venta> ventas
    ) {
        Venta primeraVenta = ventas.get(0);

        Map<String, VentaConsolidadaProductoDTO> productosMap = new LinkedHashMap<>();

        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetailsList()) {
                if (!Boolean.TRUE.equals(detalle.getActive())) {
                    continue;
                }

                Producto producto = detalle.getProduct();

                BigDecimal cantidadVendida = safe(detalle.getQuantity());
                BigDecimal cantidadDevuelta = safe(detalle.getCantidadDevuelta());
                BigDecimal cantidadFinal = cantidadVendida.subtract(cantidadDevuelta);

                if (cantidadFinal.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal precioUnitario = safe(detalle.getUnitPrice());
                BigDecimal subtotal = precioUnitario.multiply(cantidadFinal);

                Long productId = producto != null ? producto.getId() : null;
                String productName = producto != null ? producto.getName() : "Producto sin nombre";
                String unitAbbr = producto != null && producto.getUnidadMedida() != null
                        ? producto.getUnidadMedida().getAbreviatura()
                        : null;

                String key = productId + "|" + precioUnitario.stripTrailingZeros().toPlainString();

                VentaConsolidadaProductoDTO productoDTO = productosMap.computeIfAbsent(key, k -> {
                    VentaConsolidadaProductoDTO nuevo = new VentaConsolidadaProductoDTO();
                    nuevo.setProductId(productId);
                    nuevo.setProductName(productName);
                    nuevo.setUnitAbbr(unitAbbr);
                    nuevo.setQuantity(BigDecimal.ZERO);
                    nuevo.setUnitPrice(precioUnitario);
                    nuevo.setSubTotal(BigDecimal.ZERO);
                    return nuevo;
                });

                productoDTO.setQuantity(productoDTO.getQuantity().add(cantidadFinal));
                productoDTO.setSubTotal(productoDTO.getSubTotal().add(subtotal));
            }
        }

        BigDecimal total = productosMap.values()
                .stream()
                .map(VentaConsolidadaProductoDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long vendedorId = primeraVenta.getUsuario() != null
                ? primeraVenta.getUsuario().getId()
                : null;

        String vendedorNombre = primeraVenta.getUsuario() != null
                ? primeraVenta.getUsuario().getUsername()
                : "—";

        VentaConsolidadaResponseDTO response = new VentaConsolidadaResponseDTO();
        response.setClienteId(primeraVenta.getClient().getId());
        response.setClientName(primeraVenta.getClient().getName());

        response.setUserId(vendedorId);
        response.setUserName(vendedorNombre);

        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setGeneratedAt(LocalDateTime.now());

        response.setVentaIds(
                ventas.stream()
                        .map(Venta::getId)
                        .sorted()
                        .toList()
        );

        response.setTotalVentas(ventas.size());
        response.setProductos(new ArrayList<>(productosMap.values()));
        response.setTotalAmount(total);
        response.setAmountInWords(NumberToWordsConverter.convert(total));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public VentaConsolidadaResponseDTO obtenerDetalleConsolidadoPorTicket(Long weeklyTicketId) {
        if (weeklyTicketId == null) {
            throw new IllegalArgumentException("El weeklyTicketId es obligatorio");
        }

        UserContext ctx = ctx();
        Long branchId = ctx.isSuperAdmin() ? null : ctx.getBranchId();

        List<Venta> ventas = ventaRepository.findByWeeklyTicketIdFull(
                weeklyTicketId,
                branchId
        );

        if (ventas == null || ventas.isEmpty()) {
            throw new NotFoundException("No se encontró la venta consolidada");
        }

        ventas.sort(Comparator.comparing(Venta::getSaleDate));

        Venta primeraVenta = ventas.get(0);

        LocalDateTime startDate = ventas.stream()
                .map(Venta::getSaleDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(primeraVenta.getSaleDate());

        LocalDateTime endDate = ventas.stream()
                .map(Venta::getSaleDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(primeraVenta.getSaleDate());

        LocalDateTime consolidatedAt = ventas.stream()
                .map(Venta::getConsolidatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        VentaConsolidadaRequestDTO request = new VentaConsolidadaRequestDTO();
        request.setClienteId(primeraVenta.getClient().getId());

        if (primeraVenta.getUsuario() != null) {
            request.setUserId(primeraVenta.getUsuario().getId());
        }

        request.setStartDate(startDate);
        request.setEndDate(endDate);

        request.setVentaIds(
                ventas.stream()
                        .map(Venta::getId)
                        .sorted()
                        .toList()
        );

        VentaConsolidadaResponseDTO detalle = construirDetalleConsolidado(request, ventas);

        detalle.setWeeklyTicketId(weeklyTicketId);
        detalle.setConsolidatedAt(consolidatedAt);

        if (consolidatedAt != null) {
            detalle.setGeneratedAt(consolidatedAt);
        }

        return detalle;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarTicketConsolidadoPdfPorWeeklyTicketId(Long weeklyTicketId) {
        if (weeklyTicketId == null) {
            throw new IllegalArgumentException("El weeklyTicketId es obligatorio");
        }

        UserContext ctx = ctx();
        Long branchId = ctx.isSuperAdmin() ? null : ctx.getBranchId();

        List<Venta> ventas = ventaRepository.findByWeeklyTicketIdFull(
                weeklyTicketId,
                branchId
        );

        if (ventas == null || ventas.isEmpty()) {
            throw new NotFoundException("No se encontró la venta consolidada");
        }

        VentaConsolidadaResponseDTO detalle =
                obtenerDetalleConsolidadoPorTicket(weeklyTicketId);

        Sucursal branch = ventas.get(0).getBranch();

        return generatePdfService.generateVentaConsolidadaPdf(
                detalle,
                branch,
                true
        );
    }
}
