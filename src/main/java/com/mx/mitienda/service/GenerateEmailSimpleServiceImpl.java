package com.mx.mitienda.service;

import com.mx.mitienda.model.InventarioSucursal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateEmailSimpleServiceImpl implements IGenerateEmailSimpleService{
    private final TemplateEngine templateEngine;

    @Override
    public String generarHtmlAlertaStock(InventarioSucursal inventarioSucursal) {
        Context context = new Context();
        context.setVariable("sucursal",
                inventarioSucursal.getBranch());
        context.setVariable("inventarioSucursal",
                inventarioSucursal);
        context.setVariable("producto", inventarioSucursal.getProduct());

        return templateEngine.process("alerta_stock_critico", context);
    }
}
