package com.mx.mitienda.mapper;

import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlertaInventarioMapper {

    public InventarioAlertasDTO toDto(InventarioSucursal inventarioSucursal){
        InventarioAlertasDTO inventarioAlertasDTO = new InventarioAlertasDTO();
        inventarioAlertasDTO.setId(inventarioSucursal.getId());
        inventarioAlertasDTO.setProductName(inventarioSucursal.getProduct().getName());
        inventarioAlertasDTO.setBranchName(inventarioSucursal.getBranch().getName());
        inventarioAlertasDTO.setMinStock(inventarioSucursal.getMinStock());
        inventarioAlertasDTO.setMaxStock(inventarioSucursal.getMaxStock());
        inventarioAlertasDTO.setStock(inventarioSucursal.getStock());
        inventarioAlertasDTO.setStockCritico(inventarioSucursal.getStockCritico());
        return inventarioAlertasDTO;
    }

    public List<InventarioAlertasDTO> toDoList(List<InventarioSucursal> inventarioSucursalList){
        return inventarioSucursalList.stream().map(this::toDto).collect(Collectors.toList());
    }
}
