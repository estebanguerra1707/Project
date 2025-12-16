package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.*;
import com.mx.mitienda.service.CompraServiceImpl;
import com.mx.mitienda.service.IAuthenticatedUserService;
import com.mx.mitienda.service.ICompraService;
import com.mx.mitienda.service.IDevolucionComprasService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final ICompraService compraServiceImpl;
    private final IDevolucionComprasService devolucionComprasService;
    private final IAuthenticatedUserService authenticatedUserService;


    private Long effectiveBranch(Long branchIdFromQuery) {
        if (authenticatedUserService.isSuperAdmin()) {

            if (branchIdFromQuery != null) {
                return branchIdFromQuery;
            }
            return null;
        }
        return authenticatedUserService.getCurrentBranchId();
    }


    @Tag(name = "COMPRA SAVE", description = "Operaciones relacionadas con SALVAR compra ")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<CompraResponseDTO> save(@RequestBody CompraRequestDTO compra, Authentication authentication) {
        String username = authentication.getName(); // <-- viene del token
        CompraResponseDTO saved = compraServiceImpl.save(compra, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Tag(name = "COMPRA GET ALL", description = "Operaciones relacionadas con obtener todas las compras ")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<Page<CompraResponseDTO>> getPurchases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Long effectiveBranchId = effectiveBranch(branchId);

        Page<CompraResponseDTO> compras =
                compraServiceImpl.findByBranchPaginated(effectiveBranchId, page, size, sort);

        return ResponseEntity.ok(compras);
    }

    @Tag(name = "COMPRA GET ALL", description = "Operaciones relacionadas con obtener todas las compras ")
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<Page<CompraResponseDTO>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestBody CompraFiltroDTO filtro
    ) {

        Sort sortObj = Sort.by(Sort.Direction.ASC, "purchaseDate"); // valor por defecto

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0];                    // id
            Sort.Direction direction =
                    parts.length > 1
                            ? Sort.Direction.fromString(parts[1]) // asc o desc
                            : Sort.Direction.ASC;

            sortObj = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CompraResponseDTO> comprasResponse =
                compraServiceImpl.advancedSearch(filtro, pageable);

        return ResponseEntity.ok(comprasResponse);
    }

    @Tag(name = "COMPRA GET BY ID", description = "Operaciones relacionadas con obtener todas las compras ")
    @GetMapping("/{purchaseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public ResponseEntity<CompraResponseDTO> getById(@PathVariable Long purchaseId) {
        return ResponseEntity.ok(compraServiceImpl.getById(purchaseId));
    }

    @PostMapping("/devolucion")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public ResponseEntity<DevolucionComprasReponseDTO> devoluciones( @Valid @RequestBody DevolucionComprasRequestDTO devolucionComprasRequestDTO) {
        return ResponseEntity.ok(devolucionComprasService.procesarDevolucion(devolucionComprasRequestDTO));
    }

    @PostMapping("/devolucion/search")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR','SUPER_ADMIN')")
    public ResponseEntity<Page<FiltroDevolucionComprasResponseDTO>> searchDevoluciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody DevolucionComprasFiltroDTO filtro
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<FiltroDevolucionComprasResponseDTO> resp =
                devolucionComprasService.advancedSearch(filtro, pageable);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/total")
    public BigDecimal totalPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El rango es inválido: 'desde' no puede ser posterior a 'hasta'.");
        }
        return devolucionComprasService.obtenerDevolucionesComprasPorRango(desde, hasta);
    }

    @GetMapping("/devolucion-dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public Map<LocalDate, Long> porDia(@RequestParam LocalDate desde,
                                       @RequestParam LocalDate hasta,
                                       @RequestParam(required = false) Long branchId) {
        Long effectiveBranchId = effectiveBranch(branchId);
        return devolucionComprasService.contarPorDia(desde, hasta, effectiveBranchId);
    }

    @GetMapping("/devolucion-semana")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public Map<LocalDate, Long> porSemana(@RequestParam LocalDate desde,
                                       @RequestParam LocalDate hasta,
                                       @RequestParam(required = false) Long branchId) {
        Long effectiveBranchId = effectiveBranch(branchId);
        return devolucionComprasService.contarPorSemana(desde, hasta, effectiveBranchId);
    }

    @GetMapping("/devolucion-mes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'SUPER_ADMIN)")
    public Map<YearMonth, Long> PorMes(@RequestParam LocalDate desde,
                                       @RequestParam LocalDate hasta,
                                       @RequestParam(required = false) Long branchId) {
        Long effectiveBranchId = effectiveBranch(branchId);
        return devolucionComprasService.contarPorMes(desde, hasta, effectiveBranchId);
    }
    @Tag(name = "COMPRA DELETE", description = "Borrado lógico de una compra")
    @DeleteMapping("/{purchaseId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<String> deleteCompra(@PathVariable Long purchaseId) {
        compraServiceImpl.deleteLogical(purchaseId);
        return ResponseEntity.ok("Compra eliminada correctamente");
    }


}
