package com.mx.mitienda.service;

import com.mx.mitienda.mapper.MetodoPagoMapper;
import com.mx.mitienda.model.MetodoPago;
import com.mx.mitienda.model.dto.PaymentMethodDTO;
import com.mx.mitienda.model.dto.PaymentMethodResponseDTO;
import com.mx.mitienda.repository.MetodoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements IMetodoPagoService {

    private final MetodoPagoRepository paymentMethodRepository;
    private final MetodoPagoMapper paymentMethodMapper;

    @Override
    public List<PaymentMethodResponseDTO> getAll() {
        return paymentMethodRepository.findByActivoTrue().stream()
                .map(paymentMethodMapper::toResponseDTO)
                .toList();
    }

    @Override
    public PaymentMethodResponseDTO getById(Long id) {
        return paymentMethodRepository.findByIdAndActivoTrue(id)
                .map(paymentMethodMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));
    }

    @Override
    public PaymentMethodResponseDTO create(PaymentMethodDTO dto) {
        MetodoPago entity = paymentMethodMapper.toEntity(dto);
        entity.setActivo(true);
        return paymentMethodMapper.toResponseDTO(paymentMethodRepository.save(entity));
    }

    @Override
    public PaymentMethodResponseDTO update(Long id, PaymentMethodDTO dto) {
        MetodoPago entity = paymentMethodRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));
        entity.setName(dto.getName());
        return paymentMethodMapper.toResponseDTO(paymentMethodRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        MetodoPago entity = paymentMethodRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));
        entity.setActivo(false);
        paymentMethodRepository.save(entity);
    }
}
