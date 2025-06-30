package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleVentaRequestDTO;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.repository.DetalleVentaRepository;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.VentaRepository;
import com.mx.mitienda.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentasMapper {
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public Venta toEntity(VentaRequestDTO ventasRequestDTO, String username){

        // Obtener cliente
        Cliente cliente = clienteRepository.findByIdAndActiveTrue(ventasRequestDTO.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado, intenta registrarlo"));

        // Obtener usuario desde el username
        Usuario usuario = usuarioService.getByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Venta venta = new Venta();
        venta.setSaleDate(ventasRequestDTO.getSaleDate());
        log.info("ID del cliente recibido: {}", venta.getCustomer() != null ? venta.getCustomer().getId() : "null");
        venta.setCustomer(cliente);
        venta.setUsuario(usuario);
        log.info("ID del usuario recibido: {}", venta.getUsuario() != null ? venta.getUsuario().getId() : "null");
        venta.setActive(true);
        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detailsSaleList = new ArrayList<>();

        for (DetalleVentaRequestDTO detalleVentasRequestDTO : ventasRequestDTO.getDetails()) {

            Producto producto = productoRepository.findById(detalleVentasRequestDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

            if (producto.getStockQuantity() < detalleVentasRequestDTO.getQuantity()) {
                throw new RuntimeException("No hay producto suficiente para vender: " + producto.getName());
            }
            // Descontar stock
            producto.setStockQuantity(producto.getStockQuantity() - detalleVentasRequestDTO.getQuantity());

            DetalleVenta detalleVenta = new DetalleVenta();

            detalleVenta.setVenta(venta);
            detalleVenta.setProduct(producto);
            detalleVenta.setQuantity(detalleVentasRequestDTO.getQuantity());
            detalleVenta.setUnitPrice(producto.getPrice());
            detalleVenta.setSubTotal(producto.getPrice().multiply(BigDecimal.valueOf(detalleVentasRequestDTO.getQuantity())));
            detalleVenta.setActive(true);
            total = total.add(detalleVenta.getSubTotal());
            detailsSaleList.add(detalleVenta);
        }

        venta.setDetailsList(detailsSaleList);
        //  Actualizar total
        venta.setTotalAmount(total);
       // Guardar todo: venta, detalles, productos
        ventaRepository.save(venta);
        for (DetalleVenta detail : detailsSaleList) {
            productoRepository.save(detail.getProduct());
        }
        return venta;
    }

    public VentaResponseDTO toResponse(Venta venta){
        VentaResponseDTO ventaResponseDTO = new VentaResponseDTO();
        ventaResponseDTO.setId(venta.getId());
        ventaResponseDTO.setCustomerName(venta.getCustomer().getName());
        ventaResponseDTO.setSaleDate(venta.getSaleDate());
        ventaResponseDTO.setTotalAmount(venta.getTotalAmount());

        List<DetalleVentaResponseDTO> details = venta.getDetailsList().stream().map(detail->{
            DetalleVentaResponseDTO detalleVentaResponseDTO = new DetalleVentaResponseDTO();
            detalleVentaResponseDTO.setProductName(detail.getProduct().getName());
            detalleVentaResponseDTO.setQuantity(detail.getQuantity());
            detalleVentaResponseDTO.setUnitPrice(detail.getUnitPrice());
            detalleVentaResponseDTO.setSubTotal(detail.getSubTotal());
            return detalleVentaResponseDTO;
        }).toList();

        ventaResponseDTO.setDetails(details);
        return ventaResponseDTO;
    }

}
