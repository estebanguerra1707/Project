package com.mx.mitienda.service;

import com.mx.mitienda.exception.DuplicateProveedorException;
import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProveedorMapper;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.ProveedorSucursal;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.ProveedorDTO;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.repository.ProveedorRepository;
import com.mx.mitienda.repository.ProveedorSucursalRepository;
import com.mx.mitienda.repository.SucursalRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.service.base.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProveedorServiceImpl extends BaseService implements IProveedorService{


    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;
    private final ProveedorSucursalRepository proveedorSucursalRepository;
    private final SucursalRepository sucursalRepository;
    private final IProveedorSucursalService proveedorSucursalService;
    private final AuthenticatedUserServiceImpl authenticatedUserService;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository, ProveedorMapper proveedorMapper, ProveedorSucursalRepository proveedorSucursalRepository, SucursalRepository sucursalRepository, IProveedorSucursalService proveedorSucursalService, AuthenticatedUserServiceImpl authenticatedUserService) {
        super(authenticatedUserService);
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
        this.proveedorSucursalRepository = proveedorSucursalRepository;
        this.sucursalRepository = sucursalRepository;
        this.proveedorSucursalService = proveedorSucursalService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProveedorResponseDTO> getAll() {

        List<Proveedor> proveedores;

        if (authenticatedUserService.isSuperAdmin()) {
            proveedores = proveedorRepository.findAllByActiveTrue();
        } else {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new ForbiddenException("No se pudo determinar la sucursal del usuario.");
            }

            proveedores = proveedorSucursalRepository.findProveedoresActivosBySucursalId(branchId);
        }

        if (proveedores.isEmpty()) return List.of();

        List<Long> proveedorIds = proveedores.stream()
                .map(Proveedor::getId)
                .toList();

        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorIdIn(proveedorIds);

        return proveedores.stream()
                .map(p -> {
                    List<ProveedorSucursal> rels = relaciones.stream()
                            .filter(ps -> ps.getProveedor().getId().equals(p.getId()))
                            .toList();

                    return proveedorMapper.toResponse(p, rels);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProveedorResponseDTO> getByBusinessType(Long businessTypeId) {

        List<Proveedor> proveedores;

        if (authenticatedUserService.isSuperAdmin()) {
            proveedores = proveedorRepository.findByBusinessTypeId(businessTypeId);
        } else {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new ForbiddenException("Sucursal no determinada");
            }
            proveedores = proveedorRepository.findBySucursalIdAndBusinessTypeId(branchId, businessTypeId);
        }

        if (proveedores.isEmpty()) return List.of();

        List<Long> proveedorIds = proveedores.stream()
                .map(Proveedor::getId)
                .toList();

        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorIdIn(proveedorIds);

        return proveedores.stream()
                .map(proveedor -> {
                    List<ProveedorSucursal> relsProveedor = relaciones.stream()
                            .filter(ps -> ps.getProveedor().getId().equals(proveedor.getId()))
                            .toList();

                    // ADMIN: por consistencia, deja SOLO su sucursal en relaciones
                    if (!authenticatedUserService.isSuperAdmin()) {
                        Long branchId = authenticatedUserService.getCurrentBranchId();
                        relsProveedor = relsProveedor.stream()
                                .filter(ps -> ps.getSucursal().getId().equals(branchId))
                                .toList();
                    }

                    return proveedorMapper.toResponse(proveedor, relsProveedor);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ProveedorResponseDTO getById(Long idProveedor) {

        // 1️⃣ Proveedor base
        Proveedor proveedor = proveedorRepository
                .findByIdAndActiveTrue(idProveedor)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));

        // 2️⃣ Todas sus relaciones
        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorId(idProveedor);

        if (relaciones.isEmpty()) {
            throw new NotFoundException("Proveedor sin sucursales asignadas");
        }

        // 3️⃣ Si NO es super admin → filtrar a su sucursal
        if (!authenticatedUserService.isSuperAdmin()) {

            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) {
                throw new ForbiddenException("Sucursal no determinada");
            }

            relaciones = relaciones.stream()
                    .filter(ps -> ps.getSucursal().getId().equals(branchId))
                    .toList();

            if (relaciones.isEmpty()) {
                throw new NotFoundException("Proveedor no asociado a tu sucursal");
            }
        }

        // 4️⃣ Mapper centralizado (Proveedor + relaciones)
        return proveedorMapper.toResponse(proveedor, relaciones);
    }

    @Transactional
    @Override
    public ProveedorResponseDTO save(ProveedorDTO dto) {

        UserContext ctx = ctx();
        boolean isSuper = ctx.isSuperAdmin();

        if (dto.getBranchIds() == null || dto.getBranchIds().isEmpty()) {
            throw new IllegalArgumentException("El proveedor debe tener al menos una sucursal");
        }

        // ✅ VALIDACIÓN DUPLICADO SEGÚN ROL
        if (isSuper) {
            // SUPER_ADMIN: global (igual que ya lo tienes)
            proveedorRepository
                    .findByEmailAndNameAndActiveTrue(dto.getEmail(), dto.getName())
                    .ifPresent(p -> { throw new DuplicateProveedorException("Proveedor ya existe"); });

        } else {
            // ADMIN: solo valida dentro de su sucursal
            Long branchId = ctx.getBranchId();
            if (branchId == null) {
                throw new IllegalStateException("El usuario ADMIN debe tener sucursal asignada");
            }

            // (Recomendado) evita que ADMIN intente asociar proveedores a otras sucursales
            if (dto.getBranchIds().size() != 1 || !dto.getBranchIds().contains(branchId)) {
                throw new IllegalArgumentException("Solo puedes registrar el proveedor en tu sucursal");
            }

            boolean existsInMyBranch = proveedorRepository.existsActiveByBranchAndEmailAndName(
                    branchId,
                    dto.getEmail(),
                    dto.getName()
            );

            if (existsInMyBranch) {
                throw new DuplicateProveedorException("Proveedor ya existe en tu sucursal");
            }
        }
        Proveedor proveedor = proveedorRepository
                .findByEmailAndName(dto.getEmail(), dto.getName())
                .filter(p -> !p.getActive())
                .orElse(null);

        Proveedor saved;

        if (proveedor != null) {
            proveedor.setActive(true);
            proveedor.setName(dto.getName());
            proveedor.setEmail(dto.getEmail());
            proveedor.setContact(dto.getContact());

            saved = proveedorRepository.save(proveedor);

            for (Long branchId : dto.getBranchIds()) {
                Sucursal sucursal = sucursalRepository
                        .findByIdAndActiveTrue(branchId)
                        .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

                asociarProveedorASucursalSiNoExiste(saved, sucursal);
            }

        } else {
            Proveedor nuevo = proveedorMapper.toEntity(dto);
            saved = proveedorRepository.save(nuevo);

            for (Long branchId : dto.getBranchIds()) {
                Sucursal sucursal = sucursalRepository
                        .findByIdAndActiveTrue(branchId)
                        .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

                ProveedorSucursal ps = new ProveedorSucursal();
                ps.setProveedor(saved);
                ps.setSucursal(sucursal);
                proveedorSucursalRepository.save(ps);
            }
        }

        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorId(saved.getId());

        return proveedorMapper.toResponse(saved, relaciones);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO update(Long id, ProveedorDTO dto) {

        Proveedor proveedor = proveedorRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));

        if (!authenticatedUserService.isSuperAdmin()) {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) throw new ForbiddenException("Sucursal no determinada");

            boolean asociado = proveedorSucursalRepository.existsByProveedorIdAndSucursalId(proveedor.getId(), branchId);
            if (!asociado) {
                throw new ForbiddenException("No puedes actualizar un proveedor de otra sucursal");
            }
        }

        if (!proveedor.getActive()) {
            proveedor.setActive(true);
        }

        String newName  = dto.getName()  != null ? dto.getName()  : proveedor.getName();
        String newEmail = dto.getEmail() != null ? dto.getEmail() : proveedor.getEmail();

        if (newName != null && newEmail != null) {

            if (authenticatedUserService.isSuperAdmin()) {
                // SUPER_ADMIN: duplicado global
                proveedorRepository
                        .findByEmailAndNameAndIdNotAndActiveTrue(newEmail, newName, id)
                        .ifPresent(p -> { throw new DuplicateProveedorException("Proveedor ya existe"); });

            } else {
                // ADMIN: duplicado SOLO dentro de su sucursal
                Long branchId = authenticatedUserService.getCurrentBranchId();
                if (branchId == null) throw new ForbiddenException("Sucursal no determinada");

                List<Long> ids = proveedorRepository.findActiveProveedorIdsByBranchAndEmailAndName(
                        branchId,
                        newEmail,
                        newName
                );

                // Si existe un proveedor distinto al que estoy editando -> duplicado
                boolean existeOtro = ids.stream().anyMatch(pid -> !pid.equals(proveedor.getId()));
                if (existeOtro) {
                    throw new DuplicateProveedorException("Proveedor ya existe en tu sucursal");
                }
            }
        }

        if (dto.getName() != null) proveedor.setName(dto.getName());
        if (dto.getContact() != null) proveedor.setContact(dto.getContact());
        if (dto.getEmail() != null) proveedor.setEmail(dto.getEmail());

        proveedorRepository.save(proveedor);

        // 🔥 SOLO SUPER ADMIN puede tocar sucursales
        if (authenticatedUserService.isSuperAdmin() && dto.getBranchIds() != null) {

            List<ProveedorSucursal> actuales =
                    proveedorSucursalRepository.findByProveedorId(proveedor.getId());

            List<Long> actualesIds = actuales.stream()
                    .map(ps -> ps.getSucursal().getId())
                    .toList();

            List<Long> nuevasIds = dto.getBranchIds();

            // eliminar las que ya no vienen
            actuales.stream()
                    .filter(ps -> !nuevasIds.contains(ps.getSucursal().getId()))
                    .forEach(proveedorSucursalRepository::delete);

            // agregar las nuevas
            nuevasIds.stream()
                    .filter(idSucursal -> !actualesIds.contains(idSucursal))
                    .forEach(idSucursal -> {
                        Sucursal sucursal = sucursalRepository
                                .findByIdAndActiveTrue(idSucursal)
                                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

                        ProveedorSucursal ps = new ProveedorSucursal();
                        ps.setProveedor(proveedor);
                        ps.setSucursal(sucursal);
                        proveedorSucursalRepository.save(ps);
                    });
        }

        List<ProveedorSucursal> relaciones =
                proveedorSucursalRepository.findByProveedorId(proveedor.getId());

        return proveedorMapper.toResponse(proveedor, relaciones);
    }

    @Transactional
    @Override
    public void disable(Long id) {
        Proveedor proveedor = proveedorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Proveedor con Id no encontrado"));

        if (!authenticatedUserService.isSuperAdmin()) {
            Long branchId = authenticatedUserService.getCurrentBranchId();
            if (branchId == null) throw new ForbiddenException("Sucursal no determinada");

            boolean asociado = proveedorSucursalRepository.existsByProveedorIdAndSucursalId(proveedor.getId(), branchId);
            if (!asociado) {
                throw new ForbiddenException("No puedes desactivar un proveedor de otra sucursal");
            }
        }

        proveedor.setActive(false);
        proveedorRepository.save(proveedor);
    }

    private void asociarProveedorASucursalSiNoExiste(Proveedor proveedor, Sucursal sucursal) {
        boolean isProveedorSucursal = proveedorSucursalRepository
                .existsByProveedorAndSucursal(proveedor, sucursal);

        if (!isProveedorSucursal) {
            ProveedorSucursal proveedorSucursal = new ProveedorSucursal();
            proveedorSucursal.setProveedor(proveedor);
            proveedorSucursal.setSucursal(sucursal);
            proveedorSucursalRepository.save(proveedorSucursal);
        }
    }
}
