package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.repository.CompraRepository;
import com.mx.mitienda.util.CompraSpecBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraService {
    public final CompraRepository compraRepository;
    public final CompraMapper compraMapper;

    public List<CompraResponseDTO> getAll(String username, String role){
        if(role.equals(Rol.ADMIN)){
            return compraRepository.findByActiveTrueOrderByIdAsc().stream()
                    .map(compraMapper::toResponse)
                    .collect(Collectors.toList());

        }else{
            return compraRepository.findByUsuario_UsernameAndActiveTrue(username).stream()
                    .map(compraMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }


    public CompraResponseDTO getById(Long id){
        Compra compra = compraRepository.findByIdAndActiveTrue(id).orElseThrow(()->(new NotFoundException("La compra con el id:: " +id+"no se ha encontrado")));
        return compraMapper.toResponse(compra);
    }

    @Transactional
    public CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth){
        String username = auth.getName();
        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(granted -> granted.getAuthority().replace("ROLE_", ""))
                .orElse("");
        if("VENDOR".equalsIgnoreCase(rol)){
            if (compraRequestDTO.getPurchaseDate() == null ||
                    !compraRequestDTO.getPurchaseDate().equals(LocalDateTime.now())) {
                throw new IllegalArgumentException("Solo puedes registrar compras el dia de hoy");
            }
        }
        Compra compra = compraMapper.toEntity(compraRequestDTO, username);
        return compraMapper.toResponse(compraRepository.save(compra));
    }

    public void inactivePurchase(Long id) {
        Compra compra = compraRepository.findById(id).orElseThrow(()-> new NotFoundException("Compra no encontrada"));
        compra.setActive(false);
        compraRepository.save(compra);
    }

    public List<CompraResponseDTO> advancedSearch(CompraFiltroDTO compraDTO){
        Specification<Compra> spec = new CompraSpecBuilder()
                .active(compraDTO.getActive())
                .supplier(compraDTO.getSupplier())
                .dateBetween(compraDTO.getStart(), compraDTO.getEnd())
                .totalMajorTo(compraDTO.getMin())
                .totalMinorTo(compraDTO.getMax())
                .searchPerDayMonthYear(compraDTO.getDay(), compraDTO.getMonth(), compraDTO.getYear())
                .build();

        Sort sort = Sort.by(Sort.Direction.ASC, "totalAmount");


        return compraRepository.findAll(spec, sort).stream()
                .map(compraMapper::toResponse)
                .collect(Collectors.toList());
    }
}
