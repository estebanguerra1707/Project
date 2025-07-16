package com.mx.mitienda;

import com.mx.mitienda.controller.SucursalController;
import com.mx.mitienda.model.dto.SucursalResponseDTO;
import com.mx.mitienda.service.ISucursalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

        Mockito.when(sucursalService.getByBusinessType(1l)).thenReturn(List.of(sucursal));

        mockMvc.perform(MockMvcRequestBuilders.get("/sucursales"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Sucursal Centro"));
    }
}