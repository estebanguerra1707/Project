package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import com.mx.mitienda.model.dto.DevolucionesVentasFiltroDTO;
import com.mx.mitienda.model.dto.FiltroDevolucionVentasResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface IDevolucionVentasService {
    DevolucionVentasResponseDTO procesarDevolucion(DevolucionVentasRequestDTO devolucionVentasRequestDTO);
    Page<FiltroDevolucionVentasResponseDTO> findByFilter(DevolucionesVentasFiltroDTO filterDTO,
                                                         int page,
                                                         int size);
    }
