package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.model.dto.DetalleCompraResponseDTO;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.ProveedorRepository;
import com.mx.mitienda.repository.UsuarioRepository;
import com.mx.mitienda.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompraMapper {

    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public Compra toEntity(CompraRequestDTO compraRequestDTO, String userName){

        Compra compra = new Compra();
        Usuario usuario  = usuarioRepository.findByUsernameAndActiveTrue(userName).orElseThrow(()->new NotFoundException("Usuario no encontrado" + userName));
        Proveedor proveedor = proveedorRepository.findByIdAndActiveTrue(compraRequestDTO.getProviderId()).orElseThrow(()-> new NotFoundException("Proveedor no alineado con el producto, favor de verificar"));
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setActive(true);
        compra.setPurchaseDate(compraRequestDTO.getPurchaseDate());
        List<DetalleCompra> details = compraRequestDTO.getDetails().stream().map(detalleDTO->{
            Producto producto = productoRepository.findByIdAndActiveTrue(detalleDTO.getProductId())
                    .orElseThrow(()-> new NotFoundException("Producto no encontrado"));
            DetalleCompra detail = new DetalleCompra();
            detail.setQuantity(detalleDTO.getQuantity());
            BigDecimal unitCost = producto.getPrice();
            detail.setCompra(compra);
            detail.setUnitCost(unitCost);
            BigDecimal quantity = BigDecimal.valueOf(detalleDTO.getQuantity());
            BigDecimal subtotal = unitCost.multiply(quantity);
            detail.setSubTotal(subtotal);
            int newStock = producto.getStockQuantity() + detalleDTO.getQuantity();
            producto.setStockQuantity(newStock);
            detail.setProduct(producto);
            return detail;
        }).toList();
    compra.setDetails(details);
        BigDecimal total = details.stream()
                .map(DetalleCompra::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setTotalAmount(total);
        return compra;
    }

    public CompraResponseDTO toResponse(Compra compra) {
        CompraResponseDTO compraResponseDTO = new CompraResponseDTO();
        compraResponseDTO.setTotalAmount(compra.getTotalAmount());
        compraResponseDTO.setProviderName(compra.getProveedor().getName());
        compraResponseDTO.setPurchaseDate(compra.getPurchaseDate());
        List<DetalleCompraResponseDTO> details = compra.getDetails().stream().map(detail->{
            DetalleCompraResponseDTO detalleCompraResponseDTO = new DetalleCompraResponseDTO();
            detalleCompraResponseDTO.setProductName(detail.getProduct().getName());
            detalleCompraResponseDTO.setQuantity(detail.getQuantity());
            detalleCompraResponseDTO.setUnitPrice(detail.getUnitCost());
            detalleCompraResponseDTO.setSubTotal(detail.getSubTotal());
            return detalleCompraResponseDTO;
        }).toList();
    compraResponseDTO.setDetails(details);
    return compraResponseDTO;
    }
}
