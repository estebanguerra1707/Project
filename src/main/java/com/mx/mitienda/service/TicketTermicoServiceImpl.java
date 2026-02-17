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
        Venta venta = ventaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));

        StringBuilder sb = new StringBuilder();

        sb.append(center("MI INVENTARIO")).append("\n");
        sb.append(center("TICKET DE VENTA")).append("\n");
        sb.append("Fecha: ").append(LocalDate.now()).append("\n");
        sb.append(line()).append("\n");

        venta.getDetailsList().forEach(d -> {
            sb.append(d.getProduct().getName()).append("\n");
            sb.append(String.format(" %s x $%.2f   %.2f\n",
                    formatQty(d.getQuantity()),
                    d.getUnitPrice().doubleValue(),
                    d.getSubTotal().doubleValue()));
        });

        sb.append(line()).append("\n");

        sb.append("TOTAL:        $").append(money(venta.getTotalAmount())).append("\n");
        sb.append("Pago:         ").append(venta.getPaymentMethod().getName()).append("\n");
        sb.append("Recibido:     $").append(money(venta.getAmountPaid())).append("\n");
        sb.append("Cambio:       $").append(money(venta.getChangeAmount())).append("\n");
        sb.append(line()).append("\n");

        sb.append(venta.getBranch().getName()).append("\n");
        sb.append(venta.getBranch().getAddress()).append("\n");
        sb.append("Folio: ").append(venta.getId()).append("\n");
        sb.append("Atendió: ").append(venta.getUsuario().getUsername()).append("\n");

        sb.append("\n\n\n"); // Espacio para permitir corte

        return sb.toString();
    }

    public String buildCompraTicket(Long id) {
        Compra compra = compraRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException("Compra no encontrada"));

        StringBuilder sb = new StringBuilder();

        sb.append(center("MI INVENTARIO")).append("\n");
        sb.append(center("TICKET DE COMPRA")).append("\n");
        sb.append("Fecha: ").append(LocalDate.now()).append("\n");
        sb.append(line()).append("\n");

        compra.getDetails().forEach(d -> {
            sb.append(d.getProduct().getName()).append("\n");
            sb.append(" ").append(formatQty(d.getQuantity()))
                    .append(" x $").append(money(d.getUnitPrice()))
                    .append("   ").append(money(d.getSubTotal()))
                    .append("\n");
        });

        sb.append(line()).append("\n");

        sb.append("TOTAL:        $").append(money(compra.getTotalAmount())).append("\n");
        sb.append("Pago:         ").append(compra.getPaymentMethod().getName()).append("\n");
        sb.append("Recibido:     $").append(money(compra.getAmountPaid())).append("\n");
        sb.append("Cambio:       $").append(money(compra.getChangeAmount())).append("\n");
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

    private String formatQty(java.math.BigDecimal qty) {
        if (qty == null) return "0";
        return qty.stripTrailingZeros().toPlainString(); // 2, 0.5, 1.25 (bonito)
    }

    private String money(java.math.BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
