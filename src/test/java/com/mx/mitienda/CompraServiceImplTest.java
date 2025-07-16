package com.mx.mitienda.service;

import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.CompraRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CompraServiceImplTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private CompraMapper compraMapper;

    @InjectMocks
    private CompraServiceImpl compraService;

    @Test
    void shouldReturnCompraById() {
        Compra compra = new Compra();
        compra.setId(1L);

        CompraResponseDTO responseDTO = new CompraResponseDTO();
        responseDTO.setId(1L);

        when(compraRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(compra));
        when(compraMapper.toResponse(compra)).thenReturn(responseDTO);

        CompraResponseDTO result = compraService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenCompraNotFound() {
        when(compraRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> compraService.getById(99L));
    }
}