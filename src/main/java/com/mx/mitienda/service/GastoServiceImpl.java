package com.mx.mitienda.service;

import com.mx.mitienda.mapper.GastoMapper;
import com.mx.mitienda.model.Gasto;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.GastoRequestDTO;
import com.mx.mitienda.model.dto.GastoResponseDTO;
import com.mx.mitienda.repository.GastoRepository;
import com.mx.mitienda.service.base.BaseService; // IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GastoServiceImpl extends BaseService implements IGastoService { // 1. EXTENDER BaseService

    private final GastoRepository gastoRepository;
    private final GastoMapper gastoMapper;

    // 2. Inyectar IAuthenticatedUserService para mandarlo al super()
    public GastoServiceImpl(IAuthenticatedUserService authenticatedUserService,
                            GastoRepository gastoRepository,
                            GastoMapper gastoMapper) {
        super(authenticatedUserService);
        this.gastoRepository = gastoRepository;
        this.gastoMapper = gastoMapper;
    }

    @Transactional
    @Override
    public GastoResponseDTO crearGasto(GastoRequestDTO requestDTO) {
        Gasto gasto = gastoMapper.toEntity(requestDTO);

        // Obtener el contexto del usuario
        UserContext ctx = ctx();
        Long sucursalId = ctx.getBranchId();

        // 3. ASIGNAR LA SUCURSAL AL GASTO ANTES DE GUARDAR
        Sucursal sucursal = new Sucursal();
        sucursal.setId(sucursalId);
        gasto.setSucursal(sucursal);

        Gasto gastoGuardado = gastoRepository.save(gasto);
        return gastoMapper.toResponseDTO(gastoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoResponseDTO> obtenerTodosLosGastos() {
        UserContext ctx = ctx();
        Long sucursalId = ctx.getBranchId();

        // 4. FILTRAR POR SUCURSAL (Para que no vean gastos de otras tiendas)
        return gastoRepository.findBySucursal_Id(sucursalId)
                .stream()
                .map(gastoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GastoResponseDTO obtenerGastoPorId(Long id) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado con ID: " + id));
        return gastoMapper.toResponseDTO(gasto);
    }

    @Override
    @Transactional
    public GastoResponseDTO actualizarGasto(Long id, GastoRequestDTO requestDTO) {
        Gasto gastoExistente = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado con ID: " + id));

        gastoExistente.setDescripcion(requestDTO.getDescripcion());
        gastoExistente.setMonto(requestDTO.getMonto());
        gastoExistente.setFechaGasto(requestDTO.getFechaGasto());

        Gasto gastoActualizado = gastoRepository.save(gastoExistente);
        return gastoMapper.toResponseDTO(gastoActualizado);
    }

    @Override
    @Transactional
    public void eliminarGasto(Long id) {
        if (!gastoRepository.existsById(id)) {
            throw new RuntimeException("Gasto no encontrado con ID: " + id);
        }
        gastoRepository.deleteById(id);
    }
}