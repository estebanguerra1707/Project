package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.BadRequestException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.BusinessType;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.SucursalDTO;
import com.mx.mitienda.model.dto.SucursalResponseDTO;
import com.mx.mitienda.repository.BusinessTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SucursalMapper {
    private final BusinessTypeRepository businessTypeRepository;

    public Sucursal toEntity(SucursalDTO sucursalDTO){
        if (sucursalDTO.getBusinessTypeId() == null) {
            throw new BadRequestException("El tipo de negocio no debe ser nulo");
        }

        BusinessType businessType = businessTypeRepository.findById(sucursalDTO.getBusinessTypeId())
                .orElseThrow(() -> new NotFoundException(
                        "El tipo de negocio no se ha encontrado, intenta con otro."));
        Sucursal sucursal = new Sucursal();
        sucursal.setAddress(sucursalDTO.getAddress());
        sucursal.setName(sucursalDTO.getName());
        sucursal.setPhone(sucursalDTO.getPhone());
        sucursal.setAlertaStockCritico(Boolean.TRUE.equals(sucursalDTO.getIsAlertaStockCritico())); // o getAlertaStockCritico()
        sucursal.setBusinessType(businessType);
        sucursal.setActive(true);
        return sucursal;
    }

    public SucursalResponseDTO toResponse(Sucursal sucursal){
        SucursalResponseDTO sucursalResponseDTO = new SucursalResponseDTO();
        sucursalResponseDTO.setId(sucursal.getId());
        sucursalResponseDTO.setName(sucursal.getName());
        sucursalResponseDTO.setPhone(sucursal.getPhone());
        sucursalResponseDTO.setAddress(sucursal.getAddress());
        sucursalResponseDTO.setActive(sucursal.getActive());
        sucursalResponseDTO.setUsaInventarioPorDuenio(sucursal.getUsaInventarioPorDuenio());
        if (sucursal.getBusinessType() != null) {
            sucursalResponseDTO.setBusinessTypeId(sucursal.getBusinessType().getId());
            sucursalResponseDTO.setBusinessTypeName(sucursal.getBusinessType().getName());
        }
        sucursalResponseDTO.setIsAlertaStockCritico(sucursal.getAlertaStockCritico());
        return  sucursalResponseDTO;
    }

}
