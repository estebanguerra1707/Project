package com.mx.mitienda;

import com.mx.mitienda.controller.DashBoardController;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.service.IDashboardService;
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

@WebMvcTest(DashBoardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IDashboardService dashboardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnStockAlerts() throws Exception {
        InventarioAlertasDTO alerta = new InventarioAlertasDTO();
        alerta.setProductName("Lápiz");

        when(dashboardService.getTopProductos(null, null, null, null)).thenReturn((List<TopProductoDTO>) alerta);

        mockMvc.perform(get("/dashboard/stock-critico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Lápiz"));
    }
}