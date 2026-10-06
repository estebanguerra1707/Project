package com.mx.mitienda.mapper;

import com.mx.mitienda.model.Gasto;
import com.mx.mitienda.model.dto.GastoRequestDTO;
import com.mx.mitienda.model.dto.GastoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class GastoMapper {

    public Gasto toEntity(GastoRequestDTO dto) {
        if (dto == null) return null;

        Gasto gasto = new Gasto();
        gasto.setDescripcion(dto.getDescripcion());
        gasto.setMonto(dto.getMonto());
        gasto.setFechaGasto(dto.getFechaGasto());
        return gasto;
    }

    public GastoResponseDTO toResponseDTO(Gasto entity) {
        if (entity == null) return null;

        GastoResponseDTO dto = new GastoResponseDTO();
        dto.setId(entity.getId());
        dto.setDescripcion(entity.getDescripcion());
        dto.setMonto(entity.getMonto());
        dto.setFechaGasto(entity.getFechaGasto());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}