package com.mx.mitienda;

import com.mx.mitienda.model.dto.DetalleVentaRequestDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.VentaRepository;
import com.mx.mitienda.service.IVentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class VentaIntegrationTest {

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Test
    void shouldCreateVentaSuccessfully() {
        VentaRequestDTO request = new VentaRequestDTO();
        request.setBranchId(1L);
        request.setClientId(1L);
        request.setPaymentMethodId(1L);
        request.setSaleDate(LocalDateTime.now());
        request.setAmountPaid(BigDecimal.valueOf(200));

        DetalleVentaRequestDTO detalle = new DetalleVentaRequestDTO();
        detalle.setProductId(1L);
        detalle.setQuantity(2);
        request.setDetails(List.of(detalle));

        VentaResponseDTO venta = ventaService.registerSell(request, "admin@example.com");

        assertNotNull(venta.getId());
        assertEquals(1, venta.getDetails().size());
    }
}