package com.mx.mitienda.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.controller.CompraController;
import com.mx.mitienda.model.dto.CompraResponseDTO;
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

        Mockito.when(compraService.getAll(null, null)).thenReturn(List.of(compra));

        mockMvc.perform(MockMvcRequestBuilders.get("/compras")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalAmount").value(250));
    }
}