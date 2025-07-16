package com.mx.mitienda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.controller.VentaController;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.service.IVentaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

        Mockito.when(ventaService.getAll(null, null)).thenReturn(List.of(venta));

        mockMvc.perform(MockMvcRequestBuilders.get("/ventas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalAmount").value(100));
    }
}