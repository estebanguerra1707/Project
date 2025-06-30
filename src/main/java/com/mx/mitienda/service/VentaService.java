package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.DetalleVentaMapper;
import com.mx.mitienda.mapper.VentasMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.repository.DetalleVentaRepository;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.VentaRepository;
import com.mx.mitienda.util.VentaSpecBuilder;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VentasMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;

    @Transactional //si falla o hay unea excepcion en cualquier lugar del metodo, se hace rollback de todo
    public VentaResponseDTO registerSell(VentaRequestDTO request, String username) {
        Venta venta = ventaMapper.toEntity(request, username);
        return ventaMapper.toResponse(venta);
    }

    public List<VentaResponseDTO> getAll(String username, String rol) {
        List<Venta> ventas = new ArrayList<>();
        if (rol.equals(Rol.ADMIN)) {
            ventas = ventaRepository.findByActiveTrue();
        } else {
            ventas = ventaRepository.findByUsuario_UsernameAndActiveTrue(username);
        }
        return ventas.stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<VentaResponseDTO> findByFilter(VentaFiltroDTO filterDTO, String username, String role) {
        VentaSpecBuilder builder = new VentaSpecBuilder()
                .client(filterDTO.getClient())
                .dateBetween(filterDTO.getStart(), filterDTO.getEnd())
                .totalMajorTo(filterDTO.getMin())
                .totalMinorTo(filterDTO.getMax())
                .exactTotal(filterDTO.getTotal())
                .active(filterDTO.getActive())
                .sellPerDay(filterDTO.getDay())
                .sellPerMonth(filterDTO.getMonth())
                .sellPerYear(filterDTO.getYear())
                .withId(filterDTO.getId());

        if (!role.equals(Rol.ADMIN)) {
            builder.userName(username);
        }
        Specification<Venta> spec = builder.build();
        List<Venta> ventas = ventaRepository.findAll(spec); // Esto SIEMPRE devuelve Venta
        return ventas.stream()
                .map(ventaMapper::toResponse) // Aquí se convierte a VentaResponseDTO
                .collect(Collectors.toList());
    }

    public List<DetalleVentaResponseDTO> getDetailsPerSale(Long id) {
        return detalleVentaRepository.findByVenta_Id(id)
                .stream()
                .map(detalleVentaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public VentaResponseDTO getById(Long id) {
        Venta venta =  ventaRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        return ventaMapper.toResponse(venta);
    }

}
