package com.mx.mitienda.service;

import com.mx.mitienda.exception.ForbiddenException;
import com.mx.mitienda.model.Sucursal;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.repository.DetalleVentaRepository;
import com.mx.mitienda.util.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements IDashboardService{

    private final DetalleVentaRepository detalleVentaRepository;
    private final IAuthenticatedUserService authenticatedUserService;
    @Override
    public List<TopProductoDTO> getTopProductos(String groupBy, LocalDateTime start, LocalDateTime end) {
        Long branchId = authenticatedUserService.getCurrentBranchId();
        if (authenticatedUserService.isSuperAdmin()) {
            // SUPER_ADMIN ve todo
            return switch (groupBy.toLowerCase()) {
                case "day", "dia" -> detalleVentaRepository.findTopProductosPorDia(start, end);
                case "week", "semana" -> detalleVentaRepository.findTopProductosPorSemana(start, end);
                case "month", "mes" -> detalleVentaRepository.findTopProductosPorMes(start, end);
                default -> throw new IllegalArgumentException("groupBy inválido");
            };
        }

        Sucursal sucursal = authenticatedUserService.getCurrentBranch();
        if (sucursal == null) {
            throw new ForbiddenException("Tu usuario no tiene una sucursal asignada.");
        }

        return switch (groupBy.toLowerCase()) {
            case "day", "dia" -> detalleVentaRepository.findTopProductosPorDiaAndSucursal(start, end, branchId);
            case "week", "semana" -> detalleVentaRepository.findTopProductosPorSemanaAndSucursal(start, end, branchId);
            case "month", "mes" -> detalleVentaRepository.findTopProductosPorMesAndSucursal(start, end, branchId);
            default -> throw new IllegalArgumentException("groupBy inválido");
        };

    }
}
