package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.CompraMapper;
import com.mx.mitienda.model.Compra;
import com.mx.mitienda.model.DetalleCompra;
import com.mx.mitienda.model.InventarioSucursal;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.repository.InventarioSucursalRepository;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.repository.CompraRepository;
import com.mx.mitienda.util.CompraSpecBuilder;
import com.mx.mitienda.util.enums.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements ICompraService {
    public final CompraRepository compraRepository;
    public final CompraMapper compraMapper;
    private final IAuthenticatedUserService authenticatedUserService;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final IHistorialMovimientosService historialMovimientosService;
    private final IAlertaCorreoService alertaCorreoService;


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
    public CompraResponseDTO save(CompraRequestDTO compraRequestDTO, Authentication auth) {
        String username = auth.getName();
        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(granted -> granted.getAuthority().replace("ROLE_", ""))
                .orElse("");

        if ("VENDOR".equalsIgnoreCase(rol)) {
            if (compraRequestDTO.getPurchaseDate() == null ||
                    !compraRequestDTO.getPurchaseDate().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                throw new IllegalArgumentException("Solo puedes registrar compras el día de hoy");
            }
        }

        Compra compra = compraMapper.toEntity(compraRequestDTO, username);

        // Primero validamos y actualizamos inventarios
        for (DetalleCompra detalle : compra.getDetails()) {
            InventarioSucursal inventarioSucursal = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_Id(detalle.getProduct().getId(), compra.getBranch().getId())
                    .orElse(new InventarioSucursal());

            inventarioSucursal.setProduct(detalle.getProduct());
            inventarioSucursal.setBranch(compra.getBranch());

            int stockAnterior = inventarioSucursal.getStock() != null ? inventarioSucursal.getStock() : 0;
            int stockNuevo = stockAnterior + detalle.getQuantity();

            if (inventarioSucursal.getMaxStock() != null && stockNuevo > inventarioSucursal.getMaxStock()) {
                throw new IllegalArgumentException("La compra excede el inventario máximo permitido para el producto: "
                        + detalle.getProduct().getName());
            }

            if(stockNuevo <= inventarioSucursal.getMinStock()){
                inventarioSucursal.setStockCritico(true);
                if(inventarioSucursal.getBranch().getAlertaStockCritico()){
                alertaCorreoService.notificarStockCritico(inventarioSucursal);
                }else{
                    log.info(":::La sucursal no cuenta con notificaciones de stock critico enviadas por correo:::");
                    System.out.print(":::La sucursal no cuenta con notificaciones de stock critico enviadas por correo:::");
                }
            }else{
                inventarioSucursal.setStockCritico(false);
            }
            inventarioSucursal.setStock(stockNuevo);
            inventarioSucursal.setLastUpdatedBy(username);
            inventarioSucursal.setLastUpdatedDate(LocalDateTime.now());

            inventarioSucursalRepository.save(inventarioSucursal);

            // registrar historial
            historialMovimientosService.registrarMovimiento(
                    inventarioSucursal,
                    TipoMovimiento.ENTRADA,
                    detalle.getQuantity(),
                    stockAnterior,
                    stockNuevo,
                    "Compra #" + (compra.getId() != null ? compra.getId() : "pendiente")
            );
        }

        // Guardar la compra después del inventario (así evitamos guardar si algo sale mal)
        Compra compraGuardada = compraRepository.save(compra);

        return compraMapper.toResponse(compraGuardada);
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

    public List<CompraResponseDTO> findCurrentUserCompras() {
        Long branchId = authenticatedUserService.getCurrentBranchId();
        Long businessTypeId = authenticatedUserService.getCurrentBusinessTypeId();

        return compraRepository.findByBranchAndBusinessType(branchId, businessTypeId)
                .stream()
                .map(compraMapper::toResponse)
                .collect(Collectors.toList());
    }
}
