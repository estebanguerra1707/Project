package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.exception.PdfGenerationException;
import com.mx.mitienda.mapper.DetalleVentaMapper;
import com.mx.mitienda.mapper.VentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.VentaSpecBuilder;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    @Transactional //si falla o hay unea excepcion en cualquier lugar del metodo, se hace rollback de todo
    public VentaResponseDTO registerSell(VentaRequestDTO request) {
        Venta venta = ventaMapper.toEntity(request, ctx().getEmail());

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : ventaGuardada.getDetailsList()) {
            InventarioSucursal inventarioSucursal = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_Id(detalle.getProduct().getId(), ventaGuardada.getBranch().getId())
                    .orElseThrow();

            int stockAnterior = inventarioSucursal.getStock();
            int stockNuevo = stockAnterior - detalle.getQuantity(); // fue salida
            inventarioSucursal.setStockCritico(stockNuevo <= inventarioSucursal.getMinStock());
            if (stockNuevo < 0) {
                throw new IllegalArgumentException("No hay suficiente stock para vender: " + detalle.getProduct().getName());
            }

           if(stockNuevo <= inventarioSucursal.getMinStock()){
               inventarioSucursal.setStockCritico(true);
               if(inventarioSucursal.getBranch().getAlertaStockCritico()){
                   alertaCorreoService.notificarStockCritico(inventarioSucursal);
               }else{
                   log.info(":::La sucursal no cuenta con notificaciones de stock critico enviadas por correo:::");
               }
           } else {
               inventarioSucursal.setStockCritico(false);
           }

            inventarioSucursal.setStock(stockNuevo);
            inventarioSucursalRepository.save(inventarioSucursal);

            historialMovimientosService.registrarMovimiento(
                    inventarioSucursal,
                    TipoMovimiento.valueOf(TipoMovimiento.SALIDA.name()),
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

    public List<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO) {
        UserContext ctx = ctx();

        VentaSpecBuilder builder = new VentaSpecBuilder()
                .client(filterDTO.getClientName())
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

        if (!ctx.isSuperAdmin()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("branch").get("id"), ctx.getBranchId()));
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "totalAmount");
        return ventaRepository.findAll(spec, sort)
                .stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

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

    public VentaResponseDTO getById(Long id) {
        Venta venta =  ventaRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        return ventaMapper.toResponse(venta);
    }


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
    public List<VentaResponseDTO> findCurrentUserVentas() {
        UserContext ctx = ctx();
        return ventaRepository.findByBranchAndBusinessType(ctx.getBranchId(), ctx.getBusinessTypeId())
                .stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    // cálculo central [inicio, fin)
    private Totales calcularTotales(LocalDateTime inicio, LocalDateTime fin) {
        UserContext ctx = ctx();
        Long branchId = ctx.isSuperAdmin() ? null : ctx.getBranchId();

        BigDecimal ventasBrutas  = safe(ventaRepository.sumVentasBrutas(inicio, fin, branchId));
        BigDecimal devoluciones    = safe(devolucionVentasRepository.sumImporteDevuelto(inicio, fin, branchId));
        BigDecimal ventasNetas   = ventasBrutas.subtract(devoluciones);//substract es resta

        BigDecimal gananciaVentas = safe(ventaRepository.sumGananciaVentas(inicio, fin, branchId));
        BigDecimal ganaciaDevoluciones    = safe(devolucionVentasRepository.sumGananciaPerdidaPorDevoluciones(inicio, fin, branchId));
        BigDecimal gananciaNeta   = gananciaVentas.subtract(ganaciaDevoluciones);

        return new Totales(ventasBrutas, ventasNetas, gananciaNeta);
    }

    @Override
    public BigDecimal obtenerGananciaHoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59, 999_000_000); // 23:59:59.999
        return calcularTotales(inicio, fin).gananciaNeta();
    }

    @Override
    public BigDecimal obtenerGananciaSemana() {

        LocalDateTime inicio = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime fin = LocalDate.now()
                .with(DayOfWeek.SUNDAY)
                .atTime(23, 59, 59, 999_000_000);
        return calcularTotales(inicio, fin).gananciaNeta();
    }

    @Override
    public BigDecimal obtenerGananciaMes() {

        LocalDateTime inicio = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
        LocalDateTime fin = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(23, 59, 59, 999_000_000);
        return calcularTotales(inicio, fin).gananciaNeta();
    }

    @Override
    public BigDecimal obtenerGananciaPorDia(LocalDate dia) {

        LocalDateTime inicio = inicioDelDia(dia);
        LocalDateTime fin = dia.plusDays(1).atStartOfDay();
        return calcularTotales(inicio, fin).gananciaNeta();
    }


    @Override
    public BigDecimal obtenerGananciaPorRango(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta);
        return calcularTotales(inicio, fin).gananciaNeta();
    }

    @Override
    public Map<LocalDate, BigDecimal> obtenerGananciasPorDiaEnRango(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("Rango de fechas mal asignado");
        }

        Map<LocalDate, BigDecimal> resultado = new LinkedHashMap<>();
        LocalDate diaActual = desde;

        while (!diaActual.isAfter(hasta)) {
            LocalDateTime inicio = inicioDelDia(diaActual);
            LocalDateTime fin    = finDelDia(diaActual);
            BigDecimal ganancia = calcularTotales(inicio, fin).gananciaNeta();
            resultado.put(diaActual, ganancia != null ? ganancia : BigDecimal.ZERO);
            diaActual = diaActual.plusDays(1);
        }
        return resultado;
    }

    @Override
    public BigDecimal obtenerGananciaPorVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(()-> new NotFoundException("Venta no encontrada"));
        return venta.getDetailsList().stream()
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
                    BigDecimal resta =  precioVenta.subtract(precioCompra);
                    return resta.multiply(BigDecimal.valueOf(cantidadEfectiva));
                }).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
    @Override
    public BigDecimal obtenerVentasBrutasPorRango(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta); // hasta las 23:59:59.999
        return calcularTotales(inicio, fin).brutas();
    }
    @Override
    public BigDecimal obtenerVentasNetasPorRango(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = inicioDelDia(desde);
        LocalDateTime fin = finDelDia(hasta);
        return calcularTotales(inicio, fin).netas();
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
