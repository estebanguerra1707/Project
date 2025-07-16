package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.SucursalResponseDTO;
import com.mx.mitienda.service.ISucursalService;
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

@WebMvcTest(SucursalController.class)
class SucursalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISucursalService sucursalService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnSucursales() throws Exception {
        SucursalResponseDTO sucursal = new SucursalResponseDTO();
        sucursal.setId(1L);
        sucursal.setName("Sucursal Centro");

        when(sucursalService.getAll()).thenReturn(List.of(sucursal));

        mockMvc.perform(get("/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sucursal Centro"));
    }
}