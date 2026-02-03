package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.exception.PdfGenerationException;
import com.mx.mitienda.mapper.DetalleVentaMapper;
import com.mx.mitienda.mapper.VentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
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
    // pequeño contenedor inmutable para devolver 3 valores
    //es lo mismo que crear un DTO
    private record Totales(BigDecimal brutas, BigDecimal netas, BigDecimal gananciaNeta) {}

    @Override
    @Transactional //si falla o hay unea excepcion en cualquier lugar del metodo, se hace rollback de todo
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

            int stockAnterior = inventarioSucursal.getStock();
            int stockNuevo = stockAnterior - detalle.getQuantity();

            if (stockNuevo < 0) {
                throw new IllegalArgumentException(
                        "No hay suficiente stock para vender: "
                                + detalle.getProduct().getName()
                );
            }

            inventarioSucursal.setStock(stockNuevo);
            inventarioSucursal.setStockCritico(
                    inventarioSucursal.getMinStock() != null
                            && stockNuevo <= inventarioSucursal.getMinStock()
            );

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
        generateSaleOutput(venta, request.getIsPrinted(), request.getEmailList());
        return ventaMapper.toResponse(ventaRepository.save(venta));
    }
    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> getAll() {
        UserContext ctx = ctx();
        if (ctx.isSuperAdmin()) {
            return ventaRepository.findByActiveTrue()
                    .stream()
                    .map(ventaMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            return ventaRepository.findByBranch_IdAndActiveTrue(ctx.getBranchId())
                    .stream()
                    .map(ventaMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }
    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, int page, int size) {
        UserContext ctx = ctx();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        VentaSpecBuilder builder = new VentaSpecBuilder()
                .client(filterDTO.getClienteId())
                .dateBetween(filterDTO.getStartDate(), filterDTO.getEndDate())
                .totalMajorTo(filterDTO.getMin())
                .totalMinorTo(filterDTO.getMax())
                .exactTotal(filterDTO.getTotalAmount())
                .active(filterDTO.getActive())
                .sellPerDayMonthYear(filterDTO.getDay(), filterDTO.getMonth(), filterDTO.getYear())
                .sellPerMonthYear(filterDTO.getMonth(), filterDTO.getYear())
                .byPaymentMethod(filterDTO.getPaymentMethodId())
                .withId(filterDTO.getId());

        Specification<Venta> spec = builder.build();

        // Si NO es super admin → solo su branch
        if (!ctx.isSuperAdmin()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("branch").get("id"), ctx.getBranchId()));
        }

        return ventaRepository.findAll(spec, pageable)
                .map(ventaMapper::toResponse);
    }
    @Override
    public List<DetalleVentaResponseDTO> getDetailsPerSale(Long idVenta) {
        UserContext ctx = ctx();

        if (ctx.isSuperAdmin()) {
            return detalleVentaRepository.findByVenta_Id(idVenta)
                    .stream()
                    .map(detalleVentaMapper::toResponse)
                    .toList();
        }
        return detalleVentaRepository.findByVenta_IdAndVenta_Branch_Id(idVenta, ctx.getBranchId())
                .stream()
                .map(detalleVentaMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO getById(Long id) {
        Venta venta =  ventaRepository.findByIdAndActiveTrue(id)
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
    public byte[] generateTicketPdf(Long idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
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

                    BigDecimal precioVenta = detalleVenta.getUnitPrice();
                    BigDecimal precioCompra = detalleVenta.getProduct().getPurchasePrice();
                    int cantidadVendida = detalleVenta.getQuantity();

                    // Cantidad devuelta de este detalle
                    int cantidadDevuelta = Optional.ofNullable(
                            detalleDevolucionVentasRepository.sumCantidadDevuelta(detalleVenta.getId())
                    ).orElse(0);

                    // Cantidad efectiva (vendida - devuelta)
                    int cantidadEfectiva = cantidadVendida - cantidadDevuelta;

                    BigDecimal resta = precioVenta.subtract(precioCompra);

                    return resta.multiply(BigDecimal.valueOf(cantidadEfectiva));
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
}
