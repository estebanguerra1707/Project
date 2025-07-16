package com.mx.mitienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.service.IVentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IVentaService ventaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnVentas() throws Exception {
        VentaResponseDTO venta = new VentaResponseDTO();
        venta.setId(1L);
        venta.setSaleDate(LocalDateTime.now());
        venta.setTotalAmount(BigDecimal.valueOf(100));

        when(ventaService.getAll()).thenReturn(List.of(venta));

        mockMvc.perform(get("/ventas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAmount").value(100));
    }
}