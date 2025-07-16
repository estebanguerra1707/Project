package com.mx.mitienda.service;

import com.mx.mitienda.model.Producto;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.mapper.ProductoMapper;
import com.mx.mitienda.model.dto.ProductoResponseDTO;
import com.mx.mitienda.service.ProductoServiceImpl;
import com.mx.mitienda.service.IAuthenticatedUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceImplTest {

    private ProductoRepository productoRepository;
    private ProductoMapper productoMapper;
    private IAuthenticatedUserService authenticatedUserService;
    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        productoMapper = mock(ProductoMapper.class);
        authenticatedUserService = mock(IAuthenticatedUserService.class);
        productoService = new ProductoServiceImpl(productoRepository, productoMapper, authenticatedUserService, null,null, null);
    }

    @Test
    void getAll_shouldReturnListOfProductos() {
        when(authenticatedUserService.getCurrentBusinessTypeId()).thenReturn(1L);
        when(productoRepository.findByActiveTrueAndProductCategory_BusinessType_Id(eq(1L), any()))
            .thenReturn(Collections.emptyList());

        List<ProductoResponseDTO> result = productoService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}