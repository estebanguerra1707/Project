package com.mx.mitienda.service;

import com.lowagie.text.DocumentException;
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VentasMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final SucursalRepository sucursalRepository;
    private final TemplateEngine templateEngine;


    @Transactional //si falla o hay unea excepcion en cualquier lugar del metodo, se hace rollback de todo
    public VentaResponseDTO registerSell(VentaRequestDTO request, String username) {
        Venta venta = ventaMapper.toEntity(request, username);
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

}
