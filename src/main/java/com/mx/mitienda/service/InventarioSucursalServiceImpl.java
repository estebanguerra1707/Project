package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.InventarioSucursalMapper;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.repository.BusinessTypeRepository;
import com.mx.mitienda.repository.InventarioSucursalRepository;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.specification.InventarioSucursalSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioSucursalServiceImpl extends BaseService implements IInventarioSucursalService{

    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final InventarioSucursalMapper inventarioSucursalMapper;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final SucursalRepository sucursalRepository;
    private final BusinessTypeRepository businessTypeRepository;

    public InventarioSucursalServiceImpl(InventarioSucursalRepository inventarioSucursalRepository, InventarioSucursalMapper inventarioSucursalMapper, AuthenticatedUserServiceImpl authenticatedUserService, SucursalRepository sucursalRepository, BusinessTypeRepository businessTypeRepository) {
        super(authenticatedUserService);
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.inventarioSucursalMapper = inventarioSucursalMapper;
        this.authenticatedUserService = authenticatedUserService;
        this.sucursalRepository = sucursalRepository;
        this.businessTypeRepository = businessTypeRepository;
    }

    @Override
    public InventarioSucursalResponseDTO getProductoEnSucursal(Long sucursalId, Long productId) {
        var inv = inventarioSucursalRepository
                .findByBranchIdAndProductId(sucursalId, productId)
                .orElseThrow(() -> new NotFoundException("No hay inventario para este producto en esta sucursal"));

        return inventarioSucursalMapper.toResponse(inv);
    }

    @Override
    public List<InventarioSucursalResponseDTO> getProductosEnSucursal(Long sucursalId) {
        List<InventarioSucursal> inventarioList = inventarioSucursalRepository.findByBranch_Id(sucursalId);
        return inventarioList.stream().map(inventarioSucursalMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InventarioSucursalResponseDTO> getProducto(Long productId) {
        UserContext ctx = ctx();
        if (ctx.isSuperAdmin()) {
            // SUPER_ADMIN ve TODAS las sucursales
            List<InventarioSucursal> list = inventarioSucursalRepository.findByProduct_Id(productId);
            return list.stream().map(inventarioSucursalMapper::toResponse).toList();
        }

        Long branchId = authenticatedUserService.getCurrentBranchId();

        InventarioSucursal inv = inventarioSucursalRepository
                .findByProduct_IdAndBranch_Id(productId, branchId)
                .orElseThrow(() -> new NotFoundException("No hay inventario para ese producto en tu sucursal"));

        return List.of(inventarioSucursalMapper.toResponse(inv));
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

    @Override
    public List<InventarioSucursalResponseDTO> getByBusinessType(Long businessTypeId) {
        return inventarioSucursalRepository.findByBranch_BusinessType_Id(businessTypeId)
                .stream()
                .map(inventarioSucursalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventarioSucursalResponseDTO> findByBranchAndBusinessType() {
        Long currentBranchId = authenticatedUserService.getCurrentBranchId();
        Long currentBusinessType = authenticatedUserService.getBusinessTypeIdFromSession();

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(currentBranchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        BusinessType businessType = businessTypeRepository.findByIdAndActiveTrue(currentBusinessType)
                .orElseThrow(() -> new NotFoundException("Tipo de negocio no encontrado"));

        List<InventarioSucursal> inventario = inventarioSucursalRepository.findByBranchAndBusinessType(sucursal.getId(), businessType.getId());
        if (inventario.isEmpty()) {
            throw new NotFoundException(String.format(
                    "No hay inventario disponible para la sucursal '%s' con tipo de negocio '%s'",
                    sucursal.getName(),
                    businessType.getName()
            ));
        }
        return inventario.stream()
                .map(inventarioSucursalMapper::toResponse)
                .collect(Collectors.toList());

    }

    @Override
    public InventarioSucursalResponseDTO actualizarInventario(Long id, InventarioSucursalRequestDTO inventarioSucursalRequestDTO) {
        InventarioSucursal inventario = inventarioSucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventario no encontrado con id: " + id));
        inventarioSucursalMapper.updateEntity(inventario, inventarioSucursalRequestDTO);
        return inventarioSucursalMapper.toResponse(inventarioSucursalRepository.save(inventario));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<InventarioSucursalResponseDTO> search(
            InventarioGeneralfiltroDTO inventarioGeneralfiltroDTO,
            Pageable pageable
    ) {
        boolean isSuper = authenticatedUserService.isSuperAdmin();

        Long effectiveBranchId;
        Long effectiveBTId;

        if (isSuper) {
            // SUPER_ADMIN puede elegir libremente filtros desde el frontend
            effectiveBranchId = inventarioGeneralfiltroDTO.getBranchId();
            effectiveBTId = inventarioGeneralfiltroDTO.getBusinessTypeId();
        } else {
            // ADMIN / VENDOR → forzar datos de sesión
            effectiveBranchId = authenticatedUserService.getCurrentBranchId();
            effectiveBTId = authenticatedUserService.getBusinessTypeIdFromSession();
        }
        inventarioGeneralfiltroDTO.setBranchId(effectiveBranchId);
        inventarioGeneralfiltroDTO.setBusinessTypeId(effectiveBTId);
        // ✅ Usa la Specification específica para búsqueda general
        Specification<InventarioSucursal> spec =
                InventarioSucursalSpecification.searchGeneral((inventarioGeneralfiltroDTO));

        Page<InventarioSucursal> page = inventarioSucursalRepository.findAll(spec, pageable);

        return page.map(inventarioSucursalMapper::toResponse);
    }

}
