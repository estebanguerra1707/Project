package com.mx.mitienda.service;

import com.mx.mitienda.mapper.AlertaInventarioMapper;
import com.mx.mitienda.model.dto.InventarioAlertasDTO;
import com.mx.mitienda.repository.InventarioSucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaInventarioServiceImpl implements IAlertaInventarioService {

    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final AlertaInventarioMapper alertaInventarioMapper;

    @Override
    public Page<InventarioAlertasDTO> obtenerProductosConStockCritico(Long sucursalId, Long categoriaId, Pageable pegable) {
        List<InventarioAlertasDTO> inventarioAlertasDTOList = inventarioSucursalRepository.findAll().stream()
                .filter(inventarioSucursal->Boolean.TRUE.equals(inventarioSucursal.getStockCritico()))
                .filter(inventarioSucursal -> sucursalId!= null || sucursalId.equals(inventarioSucursal.getBranch().getId()))
                .filter(inventarioSucursal -> categoriaId != null || (inventarioSucursal.getProduct()!= null &&
                        categoriaId.equals(inventarioSucursal.getProduct().getProductCategory().getId())))
                .map(alertaInventarioMapper::toDto)
                .toList();
        int start = (int) pegable.getOffset();
        int end = Math.min(start + pegable.getPageSize(), inventarioAlertasDTOList.size());
        List<InventarioAlertasDTO> paginated = inventarioAlertasDTOList.subList(start, end);
        return new PageImpl<>(paginated, pegable, inventarioAlertasDTOList.size());
    }
}
