package com.mx.mitienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.service.ICompraService;
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

@WebMvcTest(CompraController.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICompraService compraService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnCompras() throws Exception {
        CompraResponseDTO compra = new CompraResponseDTO();
        compra.setId(1L);
        compra.setTotalAmount(BigDecimal.valueOf(250));
        compra.setPurchaseDate(LocalDateTime.now());

        when(compraService.getAll()).thenReturn(List.of(compra));

        mockMvc.perform(get("/compras")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAmount").value(250));
    }
}