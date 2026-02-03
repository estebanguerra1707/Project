package com.mx.mitienda.mapper;

import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.ProveedorSucursal;
import com.mx.mitienda.model.dto.ProveedorDTO;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.model.dto.SucursalAsignadaDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProveedorMapper {
    public Proveedor toEntity(ProveedorDTO proveedorDTO){
        Proveedor proveedor = new Proveedor();
        proveedor.setName(proveedorDTO.getName());
        proveedor.setEmail(proveedorDTO.getEmail());
        proveedor.setActive(true);
        proveedor.setContact(proveedorDTO.getContact());

        return proveedor;
    }

    public ProveedorResponseDTO toResponse(Proveedor proveedor,
                                           List<ProveedorSucursal> proveedorSucursales){
        ProveedorResponseDTO proveedorResponseDTO = new ProveedorResponseDTO();
        proveedorResponseDTO.setId(proveedor.getId());
        proveedorResponseDTO.setContact(proveedor.getContact());
        proveedorResponseDTO.setName(proveedor.getName());
        proveedorResponseDTO.setEmail(proveedor.getEmail());
        proveedorResponseDTO.setSucursales(
                proveedorSucursales.stream()
                        .map(ps -> {
                            SucursalAsignadaDTO s = new SucursalAsignadaDTO();
                            s.setId(ps.getSucursal().getId());
                            s.setName(ps.getSucursal().getName());
                            return s;
                        })
                        .toList()
        );
        return proveedorResponseDTO;
    }

}
