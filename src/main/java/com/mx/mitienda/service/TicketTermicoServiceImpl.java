package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.repository.CompraRepository;
import com.mx.mitienda.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TicketTermicoServiceImpl implements TicketTermicoService {

    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;

    private String center(String text) {
        int width = 32; // ancho de ticket 58mm
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private String line() {
        return "--------------------------------";
    }

    public String buildVentaTicket(Long id) {
        Venta venta = ventaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));

        StringBuilder sb = new StringBuilder();

        sb.append(center("MI INVENTARIO")).append("\n");
        sb.append(center("TICKET DE VENTA")).append("\n");
        sb.append("Fecha: ").append(LocalDate.now()).append("\n");
        sb.append(line()).append("\n");

        venta.getDetailsList().forEach(d -> {
            sb.append(d.getProduct().getName()).append("\n");
            sb.append(String.format(" %2d x $%.2f   %.2f\n",
                    d.getQuantity(),
                    d.getUnitPrice(),
                    d.getSubTotal()));
        });

        sb.append(line()).append("\n");

        sb.append(String.format("TOTAL:        $%.2f\n", venta.getTotalAmount()));
        sb.append(String.format("Pago:         %s\n", venta.getPaymentMethod().getName()));
        sb.append(String.format("Recibido:     %.2f\n", venta.getAmountPaid()));
        sb.append(String.format("Cambio:       %.2f\n", venta.getChangeAmount()));
        sb.append(line()).append("\n");

        sb.append(venta.getBranch().getName()).append("\n");
        sb.append(venta.getBranch().getAddress()).append("\n");
        sb.append("Folio: ").append(venta.getId()).append("\n");
        sb.append("Atendió: ").append(venta.getUsuario().getUsername()).append("\n");

        sb.append("\n\n\n"); // Espacio para permitir corte

        return sb.toString();
    }

    public String buildCompraTicket(Long id) {
        Compra compra = compraRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Compra no encontrada"));

        StringBuilder sb = new StringBuilder();

        sb.append(center("MI INVENTARIO")).append("\n");
        sb.append(center("TICKET DE COMPRA")).append("\n");
        sb.append("Fecha: ").append(LocalDate.now()).append("\n");
        sb.append(line()).append("\n");

        compra.getDetails().forEach(d -> {
            sb.append(d.getProduct().getName()).append("\n");
            sb.append(String.format(" %2d x $%.2f   %.2f\n",
                    d.getQuantity(),
                    d.getUnitPrice(),
                    d.getSubTotal()));
        });

        sb.append(line()).append("\n");

        sb.append(String.format("TOTAL:        $%.2f\n", compra.getTotalAmount()));
        sb.append(String.format("Pago:         %s\n", compra.getPaymentMethod().getName()));
        sb.append(String.format("Recibido:     %.2f\n", compra.getAmountPaid()));
        sb.append(String.format("Cambio:       %.2f\n", compra.getChangeAmount()));
        sb.append(line()).append("\n");

        sb.append(compra.getBranch().getName()).append("\n");
        sb.append(compra.getBranch().getAddress()).append("\n");
        sb.append("Folio: ").append(compra.getId()).append("\n");
        sb.append("Atendió: ").append(compra.getUsuario().getUsername()).append("\n");

        sb.append("\n\n\n");

        return sb.toString();
    }
    @Override
    public String generateRawTicket(String type, Long id) {
        return switch (type.toLowerCase()) {
            case "venta" -> buildVentaTicket(id);
            case "compra" -> buildCompraTicket(id);
            default -> throw new IllegalArgumentException("Tipo de ticket no soportado: " + type);
        };
    }
}
