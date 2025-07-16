package com.mx.mitienda;

import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.DetalleCompraRequestDTO;
import com.mx.mitienda.model.entity.Compra;
import com.mx.mitienda.repository.CompraRepository;
import com.mx.mitienda.service.ICompraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CompraIntegrationTest {

    @Autowired
    private ICompraService compraService;

    @Autowired
    private CompraRepository compraRepository;

    @Test
    void shouldCreateCompraSuccessfully() {
        CompraRequestDTO request = new CompraRequestDTO();
        request.setBranchId(1L);
        request.setProviderId(1L);
        request.setPaymentMethodId(1L);
        request.setPurchaseDate(LocalDateTime.now());

        DetalleCompraRequestDTO detalle = new DetalleCompraRequestDTO();
        detalle.setProductId(1L);
        detalle.setQuantity(5);
        request.setDetails(List.of(detalle));

        Compra compra = compraService.save(request, "admin@example.com");

        assertNotNull(compra.getId());
        assertEquals(1, compra.getDetailsList().size());
    }
}