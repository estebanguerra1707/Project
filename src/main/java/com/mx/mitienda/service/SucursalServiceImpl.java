package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.SucursalMapper;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.SucursalDTO;
import com.mx.mitienda.model.dto.SucursalResponseDTO;
import com.mx.mitienda.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SucursalServiceImpl implements ISucursalService {

    private final SucursalRepository sucursalRepository;
    private final SucursalMapper sucursalMapper;

    @Override
    public SucursalResponseDTO create(SucursalDTO sucursalDTO) {
        if (sucursalRepository.existsByNameIgnoreCaseAndAddressIgnoreCaseAndActiveTrue(sucursalDTO.getName(),sucursalDTO.getAddress())){
            throw new RuntimeException("Esta sucursal ya existe");
        }
        Sucursal sucursal = sucursalMapper.toEntity(sucursalDTO);
        return sucursalMapper.toResponse(sucursalRepository.save(sucursal));
    }

    @Override
    public SucursalResponseDTO update(Long id, SucursalDTO sucursalDTO) {
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(id).orElseThrow(()->new NotFoundException("La sucursal no fue encontrada"));
        if (sucursalDTO.getName() != null && !sucursalDTO.getName().isBlank()) {
            sucursal.setName(sucursalDTO.getName());
        }

        if (sucursalDTO.getAddress() != null && !sucursalDTO.getAddress().isBlank()) {
            sucursal.setAddress(sucursalDTO.getAddress());
        }

        if (sucursalDTO.getPhone() != null && !sucursalDTO.getPhone().isBlank()) {
            sucursal.setPhone(sucursalDTO.getPhone());
        }
        return sucursalMapper.toResponse(sucursalRepository.save(sucursal));
    }

    @Override
    public void disable(Long id) {
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(id).orElseThrow(()->new NotFoundException("La sucursal no fue encontrada"));
        sucursal.setActive(false);
        sucursalRepository.save(sucursal);

    }

    @Override
    public List<SucursalResponseDTO> findAll() {
        return sucursalRepository.findByActiveTrueOrderByIdAsc().stream().map(sucursalMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public SucursalResponseDTO findById(Long id) {
        return sucursalMapper.toResponse(sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada")));
    }
}
