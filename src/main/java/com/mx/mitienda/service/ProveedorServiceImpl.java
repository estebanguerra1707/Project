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
@RequiredArgsConstructor
public class ProveedorServiceImpl implements IProveedorService{


    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;
    private final ProveedorSucursalRepository proveedorSucursalRepository;
    private final SucursalRepository sucursalRepository;
    private final IProveedorSucursalService proveedorSucursalService;
    private final AuthenticatedUserServiceImpl authenticatedUserService;

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
        List<Proveedor> proveedores =
                proveedorRepository.findByBusinessTypeId(businessTypeId);
        if (proveedores.isEmpty()) {
            return List.of();
        }
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

                    return proveedorMapper.toResponse(proveedor, relsProveedor);
                })
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
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

        // 1) Si ya existe ACTIVO => duplicado
        proveedorRepository
                .findByEmailAndNameAndActiveTrue(dto.getEmail(), dto.getName())
                .ifPresent(p -> { throw new DuplicateProveedorException("Proveedor ya existe"); });

        if (dto.getBranchIds() == null || dto.getBranchIds().isEmpty()) {
            throw new IllegalArgumentException("El proveedor debe tener al menos una sucursal");
        }

        // 2) Si existe INACTIVO => reactivar y actualizar
        Proveedor proveedor = proveedorRepository
                .findByEmailAndName(dto.getEmail(), dto.getName())
                .filter(p -> !p.getActive())
                .orElse(null);

        Proveedor saved;

        if (proveedor != null) {
            // Reactivar + actualizar datos
            proveedor.setActive(true);
            proveedor.setName(dto.getName());
            proveedor.setEmail(dto.getEmail());
            proveedor.setContact(dto.getContact());

            saved = proveedorRepository.save(proveedor);

            // Asegurar relaciones con sucursales (sin duplicar)
            for (Long branchId : dto.getBranchIds()) {
                Sucursal sucursal = sucursalRepository
                        .findByIdAndActiveTrue(branchId)
                        .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

                // Reutiliza tu helper para no duplicar ProveedorSucursal
                asociarProveedorASucursalSiNoExiste(saved, sucursal);
            }

        } else {
            // 3) No existe => crear normal
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

        // 4) Respuesta con relaciones actuales
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

        if (!proveedor.getActive()) {
            proveedor.setActive(true);
        }

        String newName  = dto.getName()  != null ? dto.getName()  : proveedor.getName();
        String newEmail = dto.getEmail() != null ? dto.getEmail() : proveedor.getEmail();

        if (newName != null && newEmail != null) {
            proveedorRepository
                    .findByEmailAndNameAndIdNotAndActiveTrue(newEmail, newName, id)
                    .ifPresent(p -> {
                        throw new DuplicateProveedorException("Proveedor ya existe");
                    });
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
        Proveedor proveedor = proveedorRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new NotFoundException("Proveedor con Id no encontrado"));
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
