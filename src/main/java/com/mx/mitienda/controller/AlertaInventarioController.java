package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.InventarioAlertaFiltroDTO;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.service.IAlertaInventarioService;
import com.mx.mitienda.service.IInventarioSucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class AlertaInventarioController {

    private final IAlertaInventarioService alertaInventarioService;
    private final IInventarioSucursalService inventarioSucursalService;
    @GetMapping("/alerta")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Page<InventarioAlertasDTO> getAlertas(
            @RequestParam(required =false) Long sucursalId,
            @RequestParam(required =false) Long categoriaId,
            @RequestParam(required =false) int page,
            @RequestParam(required =false) int size
    ){

        Pageable pegable= PageRequest.of(page, size);
        return alertaInventarioService.obtenerProductosConStockCritico(sucursalId, categoriaId, pegable);
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Page<InventarioAlertasDTO> obtenerAlertas(
            @ModelAttribute InventarioAlertaFiltroDTO filtro,
            @PageableDefault(size = 10) Pageable pageable) {
        return inventarioSucursalService.obtenerAlertasStock(filtro, pageable);
    }


}
