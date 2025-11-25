package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.PaymentMethodDTO;
import com.mx.mitienda.model.dto.PaymentMethodResponseDTO;

import java.util.List;

public interface IMetodoPagoService {
    List<PaymentMethodResponseDTO> getAll();
    PaymentMethodResponseDTO getById(Long id);
    PaymentMethodResponseDTO create(PaymentMethodDTO dto);
    PaymentMethodResponseDTO update(Long id, PaymentMethodDTO dto);
    void delete(Long id);
}
