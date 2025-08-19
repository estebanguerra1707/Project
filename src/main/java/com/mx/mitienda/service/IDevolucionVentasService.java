package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.DevolucionVentasRequestDTO;
import com.mx.mitienda.model.dto.DevolucionVentasResponseDTO;
import org.springframework.security.core.Authentication;

public interface IDevolucionVentasService {
    DevolucionVentasResponseDTO procesarDevolucion(DevolucionVentasRequestDTO devolucionVentasRequestDTO, Authentication auth) ;
    }
