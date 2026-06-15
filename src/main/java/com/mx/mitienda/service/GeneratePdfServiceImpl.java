package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.exception.PdfGenerationException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.mapper.VentasMapper;
import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.CompraRepository;
import com.mx.mitienda.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import static com.mx.mitienda.util.Utils.COMPRA_CODE;
import static com.mx.mitienda.util.Utils.VENTA_CODE;
import com.mx.mitienda.model.dto.VentaConsolidadaResponseDTO;

@Service
@RequiredArgsConstructor
public class GeneratePdfServiceImpl implements IGeneratePdfService {
    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final TemplateEngine templateEngine;
    private final VentasMapper ventasMapper;
    private final CompraMapper compraMapper;

    @Override
    public byte[] generatePdf(String type, Long id, Boolean isPrinted) {
        Context context = new Context();
        String template;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

// Comunes para ambos
        context.setVariable("storeName", "Mi Inventario");
        String baseUrl = null;
        try {
            baseUrl = Paths.get("src/main/resources/static/").toUri().toURL().toString();
        } catch (MalformedURLException e) {
            throw new PdfGenerationException("No se pudo resolver la ruta base para imágenes estáticas", e);
        }

        switch (type.toLowerCase()) {
            case VENTA_CODE:
                Venta venta = ventaRepository.findByIdFull(id)
                        .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
                VentaResponseDTO ventaResponseDTO = ventasMapper.toResponse(venta);
                context.setVariable("branch", venta.getBranch());
                context.setVariable("venta", ventaResponseDTO);
                context.setVariable("fechaFormateada", venta.getSaleDate().format(formatter));
                break;

            case COMPRA_CODE:
                Compra compra = compraRepository.findByIdFull(id)
                        .orElseThrow(() -> new NotFoundException("Compra no encontrada"));
                CompraResponseDTO compraResponseDTO = compraMapper.toResponse(compra);
                context.setVariable("branch", compra.getBranch());
                context.setVariable("provider", compraResponseDTO.getProviderName());
                context.setVariable("compra", compraResponseDTO);
                context.setVariable("fechaFormateada", compra.getPurchaseDate().format(formatter));
                break;

            default:
                throw new IllegalArgumentException("Tipo de comprobante no soportado: " + type);
        }

// Asignar plantilla
        template = isPrinted
                ? String.format("ticket_thermal_%s", type.toLowerCase())
                : String.format("ticket_%s", type.toLowerCase());

        String html = templateEngine.process(template, context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new PdfGenerationException("Error generando PDF: " + type, e);
        }
    }

    @Override
    public byte[] generateVentaConsolidadaPdf(
            VentaConsolidadaResponseDTO detalle,
            Sucursal branch,
            Boolean isPrinted
    ) {
        Context context = new Context();

        DateTimeFormatter formatterDateTime =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        DateTimeFormatter formatterDate =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        context.setVariable("storeName", "Mi Inventario");
        context.setVariable("detalle", detalle);
        context.setVariable("branch", branch);
        context.setVariable("fechaGeneracion", detalle.getGeneratedAt().format(formatterDateTime));
        context.setVariable("periodoInicio", detalle.getStartDate().format(formatterDate));
        context.setVariable("periodoFin", detalle.getEndDate().format(formatterDate));

        String baseUrl;
        try {
            baseUrl = Paths.get("src/main/resources/static/").toUri().toURL().toString();
        } catch (MalformedURLException e) {
            throw new PdfGenerationException("No se pudo resolver la ruta base para imágenes estáticas", e);
        }

        String template = Boolean.TRUE.equals(isPrinted)
                ? "ticket_thermal_venta_consolidada"
                : "ticket_venta_consolidada";

        String html = templateEngine.process(template, context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new PdfGenerationException("Error generando PDF de venta consolidada", e);
        }
    }
}
