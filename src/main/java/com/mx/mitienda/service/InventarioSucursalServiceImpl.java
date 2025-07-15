package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.InventarioSucursalMapper;
import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.InventarioAlertaFiltroDTO;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.model.dto.InventarioSucursalRequestDTO;
import com.mx.mitienda.model.dto.InventarioSucursalResponseDTO;
import com.mx.mitienda.repository.InventarioSucursalRepository;
import com.mx.mitienda.specification.InventarioSucursalSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioSucursalServiceImpl  implements IInventarioSucursalService{

    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final InventarioSucursalMapper inventarioSucursalMapper;
    private final AuthenticatedUserServiceImpl authenticatedUserService;

    @Override
    public List<InventarioSucursalResponseDTO> getProductoEnSucursal(Long sucursalId, Long productId) {
        List<InventarioSucursal> inventarioSucursalList = inventarioSucursalRepository.findByBranch_IdAndProduct_IdOrderByBranch_Id(sucursalId, productId);
        if (inventarioSucursalList.isEmpty()) {
            throw new NotFoundException("No hay inventario para este producto en esta sucursal");
        }
        return inventarioSucursalList.stream().map(inventarioSucursalMapper::toResponse).toList();
    }

    @Override
    public List<InventarioSucursalResponseDTO> getProductosEnSucursal(Long sucursalId) {
        List<InventarioSucursal> inventarioList = inventarioSucursalRepository.findByBranch_Id(sucursalId);
        return inventarioList.stream().map(inventarioSucursalMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InventarioSucursalResponseDTO> getProducto(Long productId) {
        List<InventarioSucursal> inventarioList = inventarioSucursalRepository.findByProduct_Id(productId);
        return inventarioList.stream().map(inventarioSucursalMapper::toResponse)
        .collect(Collectors.toList());
    }

    @Transactional
    public InventarioSucursalResponseDTO create(InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        InventarioSucursal inv = inventarioSucursalMapper.toEntity(inventarioSucursalRequestDTO);
        InventarioSucursal saved = inventarioSucursalRepository.save(inv);
        return inventarioSucursalMapper.toResponse(saved);
    }

    @Transactional
    public void aumentarStock(Long productId, int cantidad ){
        Long branchId = authenticatedUserService.getCurrentBranchId();
        InventarioSucursal inventarioSucursal = inventarioSucursalRepository.findByProduct_IdAndBranch_Id(productId, branchId)
                .orElseThrow(()->new IllegalArgumentException("No existe inventario para este producto en esta sucursal"));
        inventarioSucursal.setStock(inventarioSucursal.getStock()+ cantidad);
        inventarioSucursalRepository.save(inventarioSucursal);

        if(inventarioSucursal.getStock()> inventarioSucursal.getMaxStock()){
            throw new IllegalArgumentException("El stock excede del monto maximo permitido");
        }
    }

    @Override
    public Page<InventarioAlertasDTO> obtenerAlertasStock(InventarioAlertaFiltroDTO filtro, Pageable pageable) {
        Specification<InventarioSucursal> spec = InventarioSucursalSpecification.conFiltros(filtro);
        Page<InventarioSucursal> page = inventarioSucursalRepository.findAll(spec, pageable);
        return page.map(inventarioSucursalMapper::toAlertDTO);
    }

    @Transactional
    public void disminuirStock(Long productId, int cantidad){
        Long branchId = authenticatedUserService.getCurrentBranchId();
        InventarioSucursal inventarioSucursal = inventarioSucursalRepository.findByProduct_IdAndBranch_Id(productId, branchId)
                .orElseThrow(()->new IllegalArgumentException("No existe inventario para este producto en esta sucursal"));
        if(inventarioSucursal.getStock()< cantidad){
            throw new IllegalArgumentException("No hay producto suficiente para realizar la venta");
        }
        inventarioSucursal.setStock(inventarioSucursal.getStock()-cantidad);
        inventarioSucursalRepository.save(inventarioSucursal);

        if(inventarioSucursal.getStock()< inventarioSucursal.getMinStock()){
            System.out.print("El producto necesita ser abastecido nuevamente, quedan pocas piezas: "+ inventarioSucursal.getProduct().getName());
        }
    }

}
