package com.mx.mitienda.mapper;

import com.mx.mitienda.model.MetodoPago;
import com.mx.mitienda.model.dto.PaymentMethodDTO;
import com.mx.mitienda.model.dto.PaymentMethodResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    MetodoPago toEntity(PaymentMethodDTO dto);
    PaymentMethodResponseDTO toResponseDTO(MetodoPago entity);
}
