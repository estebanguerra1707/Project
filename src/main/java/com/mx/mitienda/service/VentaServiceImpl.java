package com.mx.mitienda.service;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.Utils.VENTA_CODE;


@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements IVentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VentasMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final TemplateEngine templateEngine;
    private final IAuthenticatedUserService authenticatedUserService;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final IHistorialMovimientosService historialMovimientosService;
    private final IAlertaCorreoService alertaCorreoService;
    private final IGeneratePdfService generatePdfService;
    private final MailService mailService;

    @Transactional //si falla o hay unea excepcion en cualquier lugar del metodo, se hace rollback de todo
    public VentaResponseDTO registerSell(VentaRequestDTO request, String username) {
        Venta venta = ventaMapper.toEntity(request, username);

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : ventaGuardada.getDetailsList()) {
            InventarioSucursal inventarioSucursal = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_Id(detalle.getProduct().getId(), ventaGuardada.getBranch().getId())
                    .orElseThrow();

            int stockAnterior = inventarioSucursal.getStock();
            int stockNuevo = stockAnterior - detalle.getQuantity(); // fue salida
            inventarioSucursal.setStockCritico(stockNuevo < inventarioSucursal.getMinStock());
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

    public List<VentaResponseDTO> getAll(String username, String rol) {
        List<Venta> ventas = new ArrayList<>();
        if (rol.equals(Rol.ADMIN)) {
            ventas = ventaRepository.findByActiveTrue();
        } else {
            ventas = ventaRepository.findByUsuario_UsernameAndActiveTrue(username);
        }
        return ventas.stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, String username, String role) {
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

        if (!role.equals(Rol.ADMIN)) {
            builder.username(username);
        }
        Sort sort = Sort.by(Sort.Direction.ASC, "totalAmount");
        Specification<Venta> spec = builder.build();
        List<Venta> ventas = ventaRepository.findAll(spec, sort); // Esto SIEMPRE devuelve Venta
        return ventas.stream()
                .map(ventaMapper::toResponse) // Aquí se convierte a VentaResponseDTO
                .collect(Collectors.toList());
    }

    public List<DetalleVentaResponseDTO> getDetailsPerSale(Long id) {
        return detalleVentaRepository.findByVenta_Id(id)
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
        Long branchId = authenticatedUserService.getCurrentBranchId();
        Long businessTypeId = authenticatedUserService.getCurrentBusinessTypeId();

        return ventaRepository.findByBranchAndBusinessType(branchId, businessTypeId)
                .stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
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
