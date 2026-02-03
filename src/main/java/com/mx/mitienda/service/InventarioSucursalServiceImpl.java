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
import com.mx.mitienda.util.enums.InventarioOwnerType;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class InventarioSucursalServiceImpl extends BaseService implements IInventarioSucursalService{

    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final InventarioSucursalMapper inventarioSucursalMapper;
    private final AuthenticatedUserServiceImpl authenticatedUserService;
    private final SucursalRepository sucursalRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final IHistorialMovimientosService historialMovimientosService;


    public InventarioSucursalServiceImpl(InventarioSucursalRepository inventarioSucursalRepository, InventarioSucursalMapper inventarioSucursalMapper,
                                         AuthenticatedUserServiceImpl authenticatedUserService, SucursalRepository sucursalRepository,
                                         BusinessTypeRepository businessTypeRepository,
    IHistorialMovimientosService historialMovimientosService) {
        super(authenticatedUserService);
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.inventarioSucursalMapper = inventarioSucursalMapper;
        this.authenticatedUserService = authenticatedUserService;
        this.sucursalRepository = sucursalRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.historialMovimientosService = historialMovimientosService;
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioSucursalResponseDTO getProductoEnSucursal(
            Long sucursalId,
            Long productId
    ) {
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(sucursalId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        List<InventarioSucursal> inventarios =
                inventarioSucursalRepository
                        .findByProduct_IdAndBranch_Id(productId, sucursalId);

        if (inventarios.isEmpty()) {
            throw new NotFoundException("No hay inventario para este producto");
        }

        int stockTotal = inventarios.stream()
                .mapToInt(i -> i.getStock() == null ? 0 : i.getStock())
                .sum();

        InventarioSucursal base = inventarios.get(0);

        InventarioSucursalResponseDTO dto =
                inventarioSucursalMapper.toResponse(base);

        dto.setStock(stockTotal);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioSucursalResponseDTO> getProductosEnSucursal(
            Long sucursalId,
            InventarioOwnerType requestedOwnerType
    ) {
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(sucursalId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioOwnerType ownerType = resolveOwnerType(sucursal, requestedOwnerType);

        List<InventarioSucursal> inventario =
                Boolean.TRUE.equals(sucursal.getUsaInventarioPorDuenio())
                        ? inventarioSucursalRepository
                        .findByBranch_IdAndOwnerType(sucursalId, ownerType)
                        : inventarioSucursalRepository
                        .findByBranch_Id(sucursalId);

        return inventario.stream()
                .map(inventarioSucursalMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<InventarioSucursalResponseDTO> getProducto(
            Long productId,
            InventarioOwnerType requestedOwnerType
    ) {
        UserContext ctx = ctx();

        Long branchId = ctx.isSuperAdmin()
                ? null
                : authenticatedUserService.getCurrentBranchId();

        if (branchId == null) {
            if (requestedOwnerType != null) {
                return inventarioSucursalRepository
                        .findByProduct_Id(productId)
                        .stream()
                        .filter(i -> i.getOwnerType() ==
                                requestedOwnerType)
                        .map(inventarioSucursalMapper::toResponse)
                        .toList();
            }

            return inventarioSucursalRepository
                    .findByProduct_Id(productId)
                    .stream()
                    .map(inventarioSucursalMapper::toResponse)
                    .toList();
        }

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioOwnerType ownerType = resolveOwnerType(sucursal, requestedOwnerType);

        return inventarioSucursalRepository
                .findByProduct_IdAndBranch_IdAndOwnerType(
                        productId, branchId, ownerType
                )
                .map(inv -> List.of(inventarioSucursalMapper.toResponse(inv)))
                .orElse(List.of());
    }
    @Override
    @Transactional
    public InventarioSucursalResponseDTO create(
            InventarioSucursalRequestDTO dto
    ) {
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(dto.getBranchId())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        InventarioOwnerType ownerType = resolveOwnerType(sucursal, dto.getOwnerType());
        boolean exists =
                inventarioSucursalRepository
                        .findByProduct_IdAndBranch_IdAndOwnerType(
                                dto.getProductId(),
                                dto.getBranchId(),
                                ownerType
                        )
                        .isPresent();
        if (exists) {
            throw new IllegalArgumentException(
                    "Ya existe inventario para este producto, sucursal y tipo de dueño"
            );
        }
        InventarioSucursal inv = inventarioSucursalMapper.toEntity(dto);
        inv.setOwnerType(ownerType);

        if (inv.getStock() == null) {
            inv.setStock(0);
        }
        inv.setStockCritico(
                inv.getMinStock() != null && inv.getStock() < inv.getMinStock()
        );
        inv.setLastUpdatedDate(LocalDateTime.now());
        InventarioSucursal saved = inventarioSucursalRepository.save(inv);
        return inventarioSucursalMapper.toResponse(saved);
    }
    @Override
    @Transactional
    public void aumentarStock(
            Long productId,
            int cantidad,
            InventarioOwnerType requestedOwnerType
    ) {
        Long branchId = authenticatedUserService.getCurrentBranchId();

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioOwnerType ownerType =
                resolveOwnerType(sucursal, requestedOwnerType);
        InventarioSucursal inv =
                inventarioSucursalRepository
                        .findByProduct_IdAndBranch_IdAndOwnerType(
                                productId, branchId, ownerType
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe inventario para el producto con ownerType " + ownerType
                                )
                        );
        int nuevoStock = inv.getStock() + cantidad;

        if (inv.getMaxStock() != null && nuevoStock > inv.getMaxStock()) {
            throw new IllegalArgumentException("El stock excede el máximo permitido");
        }

        inv.setStock(nuevoStock);
        inv.setLastUpdatedDate(LocalDateTime.now());
        inventarioSucursalRepository.save(inv);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<InventarioAlertasDTO> obtenerAlertasStock(InventarioAlertaFiltroDTO filtro, Pageable pageable) {
        Specification<InventarioSucursal> spec = InventarioSucursalSpecification.conFiltros(filtro);
        Page<InventarioSucursal> page = inventarioSucursalRepository.findAll(spec, pageable);
        return page.map(inventarioSucursalMapper::toAlertDTO);
    }

    @Override
    @Transactional
    public void disminuirStock(
            Long productId,
            int cantidad,
            InventarioOwnerType requestedOwnerType
    ) {
        Long branchId = authenticatedUserService.getCurrentBranchId();

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioOwnerType ownerType =
                resolveOwnerType(sucursal, requestedOwnerType);

        InventarioSucursal inv =
                inventarioSucursalRepository
                        .findByProduct_IdAndBranch_IdAndOwnerType(
                                productId, branchId, ownerType
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe inventario para el producto con ownerType " + ownerType
                                )
                        );

        if (inv.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        inv.setStock(inv.getStock() - cantidad);

        if (inv.getMinStock() != null && inv.getStock() < inv.getMinStock()) {
            inv.setStockCritico(true);
        }

        inv.setLastUpdatedDate(LocalDateTime.now());
        inventarioSucursalRepository.save(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioSucursalResponseDTO> getByBusinessType(Long businessTypeId) {
        return inventarioSucursalRepository.findByBranch_BusinessType_Id(businessTypeId)
                .stream()
                .map(inventarioSucursalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional
    public InventarioSucursalResponseDTO actualizarInventario(
            Long id,
            InventarioSucursalRequestDTO dto
    ) {
        InventarioSucursal inventario = inventarioSucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventario no encontrado"));

        InventarioOwnerType originalOwnerType = inventario.getOwnerType();

        inventarioSucursalMapper.updateEntity(inventario, dto);

        inventario.setOwnerType(originalOwnerType);

        return inventarioSucursalMapper.toResponse(
                inventarioSucursalRepository.save(inventario)
        );
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
            effectiveBranchId = inventarioGeneralfiltroDTO.getBranchId();
            effectiveBTId = inventarioGeneralfiltroDTO.getBusinessTypeId();
        } else {
            effectiveBranchId = authenticatedUserService.getCurrentBranchId();
            effectiveBTId = authenticatedUserService.getBusinessTypeIdFromSession();
        }
        inventarioGeneralfiltroDTO.setBranchId(effectiveBranchId);
        inventarioGeneralfiltroDTO.setBusinessTypeId(effectiveBTId);
        Specification<InventarioSucursal> spec =
                InventarioSucursalSpecification.searchGeneral((inventarioGeneralfiltroDTO));

        Page<InventarioSucursal> page = inventarioSucursalRepository.findAll(spec, pageable);

        return page.map(inventarioSucursalMapper::toResponse);
    }

    private InventarioOwnerType resolveOwnerType(
            Sucursal sucursal,
            InventarioOwnerType requested
    ) {
        if (!Boolean.TRUE.equals(sucursal.getUsaInventarioPorDuenio())) {
            return InventarioOwnerType.PROPIO;
        }

        if (requested == null && sucursal.getUsaInventarioPorDuenio()) {
            throw new IllegalArgumentException(
                    "Debe especificarse ownerType para esta sucursal"
            );
        }

        return requested;
    }

}
