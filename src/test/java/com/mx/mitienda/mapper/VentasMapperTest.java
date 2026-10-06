package com.mx.mitienda.mapper;

import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.IAuthenticatedUserService;
import com.mx.mitienda.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class VentasMapperTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private MetodoPagoRepository metodoPagoRepository;
    @Mock private IAuthenticatedUserService authenticatedUserService;
    @Mock private BusinessTypeRepository businessTypeRepository;
    @Mock private EstadoOrdenRepository estadoOrdenRepository;

    // Mockito inyectará automáticamente todos los @Mock declarados arriba dentro de ventasMapper
    @InjectMocks
    private VentasMapper ventasMapper;

    @BeforeEach
    void setUp() {
        // La inicialización manual con new VentasMapper(null, null...) ya no es necesaria.
    }

    @Test
    void testMapperCreation() {
        // Prueba inicial para asegurar que el mapper y sus dependencias mockeadas cargaron bien
        assertNotNull(ventasMapper);
    }
}