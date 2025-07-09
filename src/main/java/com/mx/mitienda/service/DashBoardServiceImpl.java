package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.repository.DetalleVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements IDashboardService{

    private final DetalleVentaRepository detalleVentaRepository;
    @Override
    public List<TopProductoDTO> getTopProductos(String groupBy, LocalDateTime start, LocalDateTime end) {

        switch (groupBy.toLowerCase()) {
            case "day", "dia": return detalleVentaRepository.findTopProductosPorDia( start, end);
            case "week", "semana": return detalleVentaRepository.findTopProductosPorSemana(start, end);
            case "month", "mes": return detalleVentaRepository.findTopProductosPorMes(start, end);
            default: throw new IllegalArgumentException("groupBy inválido");
        }
    }
}
