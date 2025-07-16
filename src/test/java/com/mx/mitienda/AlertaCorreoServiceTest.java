package com.mx.mitienda.service;

import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.repository.InventarioSucursalRepository;
import com.mx.mitienda.service.impl.AlertaCorreoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaCorreoServiceTest {

    @Mock
    private MailService mailService;

    @InjectMocks
    private AlertaCorreoServiceImpl alertaCorreoService;

    @Test
    void shouldSendEmailWhenStockIsCritical() {
        InventarioSucursal inventario = new InventarioSucursal();
        inventario.setStockCritico(true);

        alertaCorreoService.notificarStockCritico(inventario);

        verify(mailService, times(1)).sendPDFEmail(anyList(), anyString(), anyString(), anyString(), any(), anyString());
    }
}