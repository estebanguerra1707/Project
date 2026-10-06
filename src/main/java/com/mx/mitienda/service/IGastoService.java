package com.mx.mitienda.service;

import com.mx.mitienda.model.dto.GastoRequestDTO;
import com.mx.mitienda.model.dto.GastoResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IGastoService {
    @Transactional
    GastoResponseDTO crearGasto(GastoRequestDTO requestDTO);

    //    GastoResponseDTO crearGasto(GastoRequestDTO requestDTO);
    List<GastoResponseDTO> obtenerTodosLosGastos();
    GastoResponseDTO obtenerGastoPorId(Long id);
    GastoResponseDTO actualizarGasto(Long id, GastoRequestDTO requestDTO);
    void eliminarGasto(Long id);
}