package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.InventarioSucursal;

import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaCorreoServiceImpl implements IAlertaCorreoService{

    private final MailService mailService;

    @Value("${alertas.stock.email.destinatario}")
    private String destinatario;
    @Value("${alertas.stock.email.origen}")
    private String origen;

    private final IGenerateEmailSimpleService generateEmailSimpleService;

    @Override
    public void notificarStockCritico(InventarioSucursal inventario ) {
            String email = generateEmailSimpleService.generarHtmlAlertaStock(inventario);
            String alerta = "Alerta! Stock Crítico: " + inventario.getProduct().getName();
            mailService.sendSimpleEmail(origen, destinatario, alerta, email);
    }
}
