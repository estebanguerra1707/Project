package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.TopProductoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface IDashboardService {
    List<TopProductoDTO> getTopProductos(String groupBy, LocalDateTime start, LocalDateTime end);
}
