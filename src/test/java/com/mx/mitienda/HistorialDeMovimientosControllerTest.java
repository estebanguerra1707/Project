package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.HistorialMovimientoResponseDTO;
import com.mx.mitienda.service.IHistorialMovimientosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistorialDeMovimientosController.class)
class HistorialDeMovimientosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IHistorialMovimientosService historialMovimientosService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnMovimientos() throws Exception {
        HistorialMovimientoResponseDTO movimiento = new HistorialMovimientoResponseDTO();
        movimiento.setProductName("Producto 1");

        when(historialMovimientosService.findAllByBranch()).thenReturn(List.of(movimiento));

        mockMvc.perform(get("/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Producto 1"));
    }
}