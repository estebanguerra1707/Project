package com.mx.mitienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.service.IProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProveedorController.class)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProveedorService proveedorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnProveedores() throws Exception {
        ProveedorResponseDTO proveedor = new ProveedorResponseDTO();
        proveedor.setId(1L);
        proveedor.setName("Proveedor Test");

        when(proveedorService.getAll()).thenReturn(List.of(proveedor));

        mockMvc.perform(get("/proveedores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Proveedor Test"));
    }
}