package com.mx.mitienda;

import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.service.IInventarioSucursalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StockCriticoDashboardTest {

    @Autowired
    private IInventarioSucursalService inventarioSucursalService;

    @Test
    void shouldReturnCriticalStockItems() {
        List<InventarioAlertasDTO> items = inventarioSucursalService.findCriticalStockItems();
        assertNotNull(items);
    }
}