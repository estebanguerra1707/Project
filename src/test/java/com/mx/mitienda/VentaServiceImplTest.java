package com.mx.mitienda.service;

import com.mx.mitienda.mapper.VentasMapper;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private VentasMapper ventasMapper;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    void shouldReturnVentaById() {
        Venta venta = new Venta();
        venta.setId(1L);

        VentaResponseDTO responseDTO = new VentaResponseDTO();
        responseDTO.setId(1L);

        when(ventaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(venta));
        when(ventasMapper.toResponse(venta)).thenReturn(responseDTO);

        VentaResponseDTO result = ventaService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenVentaNotFound() {
        when(ventaRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> ventaService.getById(99L));
    }
}