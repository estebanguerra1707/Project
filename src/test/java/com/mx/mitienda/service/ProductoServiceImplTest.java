package com.mx.mitienda.service;

import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.Producto;
import com.mx.mitienda.model.dto.ProductoFiltroDTO;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock private ProductoMapper productoMapper;
    @Mock private IAuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private ProductoServiceImpl productoService; // Mockito arma el constructor correcto

    @Test
    void getAll_shouldReturnEmptyPage_forSuperAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        // usuario super admin
        when(authenticatedUserService.isSuperAdmin()).thenReturn(true);
        when(authenticatedUserService.getCurrentBusinessTypeId()).thenReturn(1L);

        // Act
        Page<ProductoResponseDTO> result = productoService.getAll(new ProductoFiltroDTO(), pageable);

        // Assert
        assertTrue(result.isEmpty());

    }

    @Test
    void getAll_shouldFilterByBranch_forVendor() {
        Pageable pageable = PageRequest.of(0, 5);

        when(authenticatedUserService.isSuperAdmin()).thenReturn(false);
        when(authenticatedUserService.getCurrentBusinessTypeId()).thenReturn(2L);
        when(authenticatedUserService.getCurrentBranchId()).thenReturn(99L);

        Producto p = new Producto(); p.setId(1L);
        ProductoResponseDTO dto = new ProductoResponseDTO(); dto.setId(1L);

        when(productoMapper.toResponse(p)).thenReturn(dto);

        Page<ProductoResponseDTO> result = productoService.getAll(new ProductoFiltroDTO(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}
