package com.mx.mitienda.mapper;

import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.SucursalDTO;
import com.mx.mitienda.model.dto.SucursalResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class SucursalMapper {

    public Sucursal toEntity(SucursalDTO sucursalDTO){
        Sucursal sucursal = new Sucursal();
        sucursal.setAddress(sucursalDTO.getAddress());
        sucursal.setName(sucursalDTO.getName());
        sucursal.setPhone(sucursalDTO.getPhone());
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
        return  sucursalResponseDTO;
    }

}
