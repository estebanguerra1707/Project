package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ClienteMapper;
import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.ClienteSucursal;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.repository.ClienteSucursalRepository;
import com.mx.mitienda.service.base.BaseService;
import com.mx.mitienda.util.ClienteSpecBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl extends BaseService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ClienteSucursalRepository clienteSucursalRepository;
    private final ISucursalService sucursalService;

    private static final String CLIENTE_REL_FIELD = "clienteSucursales";

    public ClienteServiceImpl(
            IAuthenticatedUserService authenticatedUserService,
            ClienteRepository clienteRepository,
            ClienteMapper clienteMapper,
            ClienteSucursalRepository clienteSucursalRepository,
            ISucursalService sucursalService
    ) {
        super(authenticatedUserService);
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.clienteSucursalRepository = clienteSucursalRepository;
        this.sucursalService = sucursalService;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClienteResponseDTO> getAll() {
        UserContext c = ctx();
        if (c.isSuperAdmin()) {
            List<ClienteSucursal> rels = clienteSucursalRepository.findByActiveTrue();
            if (rels.isEmpty()) return List.of();

            return rels.stream()
                    .collect(Collectors.groupingBy(cs -> cs.getCliente().getId()))
                    .values()
                    .stream()
                    .map(group -> {
                        Cliente cliente = group.get(0).getCliente();

                        ClienteResponseDTO dto = clienteMapper.toResponse(cliente);
                        dto.setSucursalId(null);

                        List<Long> branchIds = group.stream()
                                .map(cs -> cs.getSucursal().getId())
                                .distinct()
                                .toList();

                        dto.setBranchIds(branchIds);
                        dto.setSucursalesCount((long) branchIds.size());
                        dto.setMultiSucursal(branchIds.size() > 1);

                        return dto;
                    })
                    .toList();
        }
        Long branchId = c.getBranchId();
        if (branchId == null) throw new ForbiddenException("No se pudo determinar la sucursal del usuario.");

        return clienteSucursalRepository.findBySucursalIdAndActiveTrue(branchId)
                .stream()
                .map(ClienteSucursal::getCliente)
                .distinct()
                .map(cliente -> enrich(cliente, branchId))
                .toList();
    }

    @Override
    public Cliente getClienteEntityById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente con id:::" + id + " no encontrado"));
    }

    @Transactional(readOnly = true)
    @Override
    public ClienteResponseDTO getById(Long id) {
        UserContext c = ctx();

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        if (c.isSuperAdmin()) {
            List<ClienteSucursal> rels = clienteSucursalRepository.findByClienteIdAndActiveTrue(id);
            if (rels.isEmpty()) {
                throw new NotFoundException("Cliente sin sucursales activas asignadas");
            }

            ClienteResponseDTO dto = clienteMapper.toResponse(cliente);
            dto.setSucursalId(null);

            List<Long> branchIds = getActiveBranchIds(id);
            dto.setBranchIds(branchIds);
            dto.setSucursalesCount((long) branchIds.size());
            dto.setMultiSucursal(branchIds.size() > 1);

            return dto;
        }
        Long branchId = c.getBranchId();
        if (branchId == null) throw new ForbiddenException("Sucursal no determinada");

        clienteSucursalRepository.findBySucursalIdAndClienteIdAndActiveTrue(branchId, id)
                .orElseThrow(() -> new NotFoundException("Cliente no asociado a tu sucursal"));

        return enrich(cliente, branchId);
    }

    @Transactional
    @Override
    public ClienteResponseDTO save(ClienteDTO dto) {

        UserContext c = ctx();

        final List<Long> targetBranchIds;

        if (c.isSuperAdmin()) {
            List<Long> ids = resolveBranchIdsForSuperAdmin(dto);

            if (ids.isEmpty()) {
                throw new IllegalArgumentException("SUPER_ADMIN debe enviar al menos una sucursal (branchIds o branchId)");
            }

            ids.forEach(sucursalService::findById);
            targetBranchIds = ids;

        } else {
            Long branchId = c.getBranchId();
            if (branchId == null) throw new ForbiddenException("No se pudo determinar la sucursal del usuario.");
            targetBranchIds = List.of(branchId);
        }

        Optional<Cliente> existingOpt = findClienteByIdentity(dto);

        Cliente saved;

        if (existingOpt.isPresent()) {
            Cliente existing = existingOpt.get();

            if (Boolean.FALSE.equals(existing.getActive())) {
                existing.setActive(true);
            }

            clienteMapper.updateEntity(existing, dto);
            saved = clienteRepository.save(existing);

        } else {
            Cliente nuevo = clienteMapper.toEntity(dto);
            nuevo.setActive(true);
            saved = clienteRepository.save(nuevo);
        }
        for (Long branchId : targetBranchIds) {
            upsertClienteSucursal(saved.getId(), branchId);
        }

        ClienteResponseDTO response = clienteMapper.toResponse(saved);
        response.setSucursalId(c.isSuperAdmin() ? null : targetBranchIds.get(0));

        List<Long> branchIds = getActiveBranchIds(saved.getId());
        response.setBranchIds(branchIds);
        response.setSucursalesCount((long) branchIds.size());
        response.setMultiSucursal(branchIds.size() > 1);

        return response;
    }

    @Transactional
    @Override
    public void disableClient(Long clienteId) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        if (Boolean.TRUE.equals(cliente.getActive())) {
            cliente.setActive(false);
            clienteRepository.save(cliente);
        }

        List<ClienteSucursal> rels = clienteSucursalRepository.findByClienteId(clienteId);

        if (!rels.isEmpty()) {
            rels.forEach(r -> r.setActive(false));
            clienteSucursalRepository.saveAll(rels);
        }
    }

    @Transactional
    @Override
    public ClienteResponseDTO updateClient(Long id, ClienteDTO dto) {

        UserContext c = ctx();

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        if (Boolean.FALSE.equals(cliente.getActive())) {
            cliente.setActive(true);
        }
        if (!c.isSuperAdmin()) {

            Long branchId = c.getBranchId();
            if (branchId == null) throw new ForbiddenException("Sucursal no determinada");

            clienteSucursalRepository.findBySucursalIdAndClienteIdAndActiveTrue(branchId, id)
                    .orElseThrow(() -> new NotFoundException("Cliente no asociado a tu sucursal"));

            clienteMapper.updateEntity(cliente, dto);
            Cliente saved = clienteRepository.save(cliente);

            return enrich(saved, branchId);
        }
        clienteMapper.updateEntity(cliente, dto);
        Cliente saved = clienteRepository.save(cliente);
        if (dto.getBranchIds() != null || dto.getBranchId() != null) {

            final List<Long> nuevasIds = resolveBranchIdsForSuperAdmin(dto);

            if (!nuevasIds.isEmpty()) {
                nuevasIds.forEach(sucursalService::findById);
                List<ClienteSucursal> actuales = clienteSucursalRepository.findByClienteIdAndActiveTrue(id);
                for (ClienteSucursal cs : actuales) {
                    Long sid = cs.getSucursal().getId();
                    if (!nuevasIds.contains(sid)) {
                        cs.setActive(false);
                        clienteSucursalRepository.save(cs);
                    }
                }
                for (Long branchId : nuevasIds) {
                    upsertClienteSucursal(id, branchId);
                }
            }
        }

        ClienteResponseDTO response = clienteMapper.toResponse(saved);
        response.setSucursalId(null);

        List<Long> branchIds = getActiveBranchIds(id);
        response.setBranchIds(branchIds);
        response.setSucursalesCount((long) branchIds.size());
        response.setMultiSucursal(branchIds.size() > 1);

        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClienteResponseDTO> advancedSearch(ClienteFiltroDTO filtro) {

        UserContext c = ctx();

        if (c.isSuperAdmin() && (filtro == null || filtro.getBranchId() == null)) {

            Specification<Cliente> spec = new ClienteSpecBuilder()
                    .active(filtro != null ? filtro.getActive() : null)
                    .name(filtro != null ? filtro.getName() : null)
                    .email(filtro != null ? filtro.getEmail() : null)
                    .phoneNumber(filtro != null ? filtro.getPhone() : null)
                    .withId(filtro != null ? filtro.getId() : null)
                    .build();

            Specification<Cliente> specTieneSucursalActiva =
                    (root, query, cb) -> cb.isTrue(root.join(CLIENTE_REL_FIELD).get("active"));

            return clienteRepository.findAll(spec.and(specTieneSucursalActiva))
                    .stream()
                    .map(cli -> {
                        ClienteResponseDTO dto = clienteMapper.toResponse(cli);
                        dto.setSucursalId(null);

                        List<Long> branchIds = getActiveBranchIds(cli.getId());
                        dto.setBranchIds(branchIds);
                        dto.setSucursalesCount((long) branchIds.size());
                        dto.setMultiSucursal(branchIds.size() > 1);

                        return dto;
                    })
                    .toList();
        }
        Long branchId = c.isSuperAdmin()
                ? filtro.getBranchId()
                : c.getBranchId();

        if (branchId == null) throw new ForbiddenException("Sucursal no determinada");
        if (c.isSuperAdmin()) sucursalService.findById(branchId);

        Specification<Cliente> spec = new ClienteSpecBuilder()
                .active(filtro.getActive())
                .name(filtro.getName())
                .email(filtro.getEmail())
                .phoneNumber(filtro.getPhone())
                .withId(filtro.getId())
                .build();

        Specification<Cliente> specSucursal = (root, query, cb) -> {
            var join = root.join(CLIENTE_REL_FIELD);
            return cb.and(
                    cb.equal(join.get("sucursal").get("id"), branchId),
                    cb.isTrue(join.get("active"))
            );
        };

        return clienteRepository.findAll(spec.and(specSucursal))
                .stream()
                .map(cli -> enrich(cli, branchId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> advancedSearchPage(
            ClienteFiltroDTO filtro,
            Pageable pageable
    ) {
        UserContext c = ctx();

        Specification<Cliente> spec = new ClienteSpecBuilder()
                .active(filtro.getActive())
                .name(filtro.getName())
                .email(filtro.getEmail())
                .phoneNumber(filtro.getPhone())
                .withId(filtro.getId())
                .build();

        if (c.isSuperAdmin() && filtro.getBranchId() == null) {

            Specification<Cliente> hasActiveSucursal =
                    (root, q, cb) -> cb.isTrue(root.join(CLIENTE_REL_FIELD).get("active"));

            Page<Cliente> page = clienteRepository.findAll(
                    spec.and(hasActiveSucursal),
                    pageable
            );

            return mapToPage(page, null);
        }

        Long branchId = c.isSuperAdmin()
                ? filtro.getBranchId()
                : c.getBranchId();

        if (branchId == null) throw new ForbiddenException("Sucursal no determinada");
        if (c.isSuperAdmin()) sucursalService.findById(branchId);

        Specification<Cliente> specSucursal = (root, q, cb) -> {
            var join = root.join(CLIENTE_REL_FIELD);
            return cb.and(
                    cb.equal(join.get("sucursal").get("id"), branchId),
                    cb.isTrue(join.get("active"))
            );
        };

        Page<Cliente> page = clienteRepository.findAll(
                spec.and(specSucursal),
                pageable
        );

        return mapToPage(page, branchId);
    }

    private ClienteResponseDTO enrich(Cliente cliente, Long branchId) {
        List<Long> branchIds = getActiveBranchIds(cliente.getId());

        ClienteResponseDTO dto = clienteMapper.toResponse(cliente);
        dto.setSucursalId(branchId);

        dto.setBranchIds(branchIds);
        dto.setSucursalesCount((long) branchIds.size());
        dto.setMultiSucursal(branchIds.size() > 1);

        return dto;
    }

    private Optional<Cliente> findClienteByIdentity(ClienteDTO dto) {
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            return clienteRepository.findFirstByEmailIgnoreCase(dto.getEmail().trim());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            return clienteRepository.findFirstByPhone(dto.getPhone().trim());
        }
        return Optional.empty();
    }

    private void upsertClienteSucursal(Long clienteId, Long branchId) {
        sucursalService.findById(branchId);
        Optional<ClienteSucursal> relOpt = clienteSucursalRepository.findBySucursalIdAndClienteId(branchId, clienteId);

        if (relOpt.isPresent()) {
            ClienteSucursal rel = relOpt.get();
            rel.setActive(true);
            clienteSucursalRepository.save(rel);
            return;
        }
        Cliente clienteRef = new Cliente();
        clienteRef.setId(clienteId);

        Sucursal sucRef = new Sucursal();
        sucRef.setId(branchId);

        ClienteSucursal rel = new ClienteSucursal();
        rel.setCliente(clienteRef);
        rel.setSucursal(sucRef);
        rel.setActive(true);

        clienteSucursalRepository.save(rel);
    }

    private List<Long> resolveBranchIdsForSuperAdmin(ClienteDTO dto) {
        if (dto.getBranchIds() != null && !dto.getBranchIds().isEmpty()) {
            return dto.getBranchIds();
        }
        if (dto.getBranchId() != null) {
            return List.of(dto.getBranchId());
        }
        return List.of();
    }

    private List<Long> getActiveBranchIds(Long clienteId) {
        return clienteSucursalRepository
                .findByClienteIdAndActiveTrue(clienteId)
                .stream()
                .map(cs -> cs.getSucursal().getId())
                .distinct()
                .toList();
    }
    private Page<ClienteResponseDTO> mapToPage(
            Page<Cliente> page,
            Long visibleBranchId
    ) {
        List<Long> clienteIds = page.getContent()
                .stream()
                .map(Cliente::getId)
                .toList();

        var counts = clienteSucursalRepository
                .countActiveSucursalesByClienteIds(clienteIds)
                .stream()
                .collect(Collectors.toMap(
                        ClienteSucursalRepository.ClienteSucursalCount::getClienteId,
                        ClienteSucursalRepository.ClienteSucursalCount::getCnt
                ));

        var branchMap = clienteSucursalRepository
                .findActiveSucursalIdsByClienteIds(clienteIds)
                .stream()
                .collect(Collectors.groupingBy(
                        r -> (Long) r[0],
                        Collectors.mapping(r -> (Long) r[1], Collectors.toList())
                ));

        return page.map(cliente -> {
            List<Long> branchIds = branchMap.getOrDefault(cliente.getId(), List.of());
            long count = counts.getOrDefault(cliente.getId(), 0L);
            ClienteResponseDTO dto = clienteMapper.toResponse(cliente);
            dto.setSucursalId(visibleBranchId);
            dto.setBranchIds(branchIds);
            dto.setSucursalesCount(count);
            dto.setMultiSucursal(count > 1);
            return dto;
        });
    }
}