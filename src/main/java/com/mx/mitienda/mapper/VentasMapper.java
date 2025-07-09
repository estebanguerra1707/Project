package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleVentaRequestDTO;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.UsuarioService;
import com.mx.mitienda.util.NumberToWordsConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.mx.mitienda.util.enums.TipoPago.EFECTIVO;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentasMapper {
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public Venta toEntity(VentaRequestDTO ventasRequestDTO, String username){

        // Obtener cliente
        Cliente cliente = clienteRepository.findByIdAndActiveTrue(ventasRequestDTO.getClientId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado, intenta registrarlo"));

        // Obtener usuario desde el username
        Usuario usuario = usuarioService.getByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(ventasRequestDTO.getBranchId()).orElseThrow(()->new NotFoundException("Sucursal no encontrada"));

        // Asigna PaymentMethod
        MetodoPago paymentMethod = metodoPagoRepository.findById(ventasRequestDTO.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado"));

        Venta venta = new Venta();
        venta.setSaleDate(ventasRequestDTO.getSaleDate());
        log.info("ID del cliente recibido: {}", venta.getClient() != null ? venta.getClient().getId() : "null");
        venta.setClient(cliente);
        venta.setUsuario(usuario);
        venta.setBranch(sucursal);
        log.info("ID del usuario recibido: {}", venta.getUsuario() != null ? venta.getUsuario().getId() : "null");
        venta.setActive(true);
        venta.setPaymentMethod(paymentMethod);


        if(venta.getPaymentMethod().getName().equalsIgnoreCase(EFECTIVO.name().toLowerCase())){
            venta.setAmountPaid(ventasRequestDTO.getAmountPaid());
        }else{
            venta.setAmountPaid(BigDecimal.ZERO);
        }

        List<DetalleVenta> details = ventasRequestDTO.getDetails().stream().map(detalleVentaRequestDTO -> {
            Producto producto = productoRepository.findById(detalleVentaRequestDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
            InventarioSucursal inventarioSucursal = inventarioSucursalRepository
                    .findByProduct_IdAndBranch_Id(producto.getId(), sucursal.getId())
                    .orElseThrow(()-> new NotFoundException("No hay inventario para este producto en esta sucursal"));
            int currentStock = inventarioSucursal.getQuantity()!=null ? inventarioSucursal.getQuantity():0;
            if(currentStock < detalleVentaRequestDTO.getQuantity()){
                throw new RuntimeException("No hay producto suficiente para vender: " + producto.getName());
            }
            // Descontar stock
            inventarioSucursal.setQuantity(currentStock-detalleVentaRequestDTO.getQuantity());
            inventarioSucursalRepository.save(inventarioSucursal);

            DetalleVenta detalleVenta = new DetalleVenta();
            detalleVenta.setVenta(venta);
            detalleVenta.setProduct(producto);
            detalleVenta.setQuantity(detalleVentaRequestDTO.getQuantity());
            detalleVenta.setUnitPrice(producto.getPurchasePrice());
            detalleVenta.setSubTotal(producto.getPurchasePrice().multiply(BigDecimal.valueOf(detalleVentaRequestDTO.getQuantity())));
            detalleVenta.setActive(true);

            return detalleVenta;
        }).toList();

        venta.setDetailsList(details);
        //  Actualizar total
        BigDecimal total = details.stream().map(DetalleVenta::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        venta.setTotalAmount(total);

        if(venta.getPaymentMethod().getName().equalsIgnoreCase(EFECTIVO.name().toLowerCase())){
            if (venta.getAmountPaid().compareTo(total) < 0) {
                throw new IllegalArgumentException("El monto pagado no puede ser menor al total.");
            }
            BigDecimal change = ventasRequestDTO.getAmountPaid().subtract(total);
            venta.setChangeAmount(change.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : change);
        }else {
            venta.setChangeAmount(BigDecimal.ZERO);
        }
        return venta;
    }

    public VentaResponseDTO toResponse(Venta venta){
        VentaResponseDTO ventaResponseDTO = new VentaResponseDTO();
        ventaResponseDTO.setId(venta.getId());
        ventaResponseDTO.setClientName(venta.getClient().getName());
        ventaResponseDTO.setSaleDate(venta.getSaleDate());
        ventaResponseDTO.setTotalAmount(venta.getTotalAmount());
        ventaResponseDTO.setPaymentMethodId(venta.getPaymentMethod().getId());
        ventaResponseDTO.setPaymentName(venta.getPaymentMethod().getName());
        ventaResponseDTO.setAmountInWords(NumberToWordsConverter.convert(venta.getTotalAmount()));
        ventaResponseDTO.setChangeAmount(venta.getChangeAmount());
        ventaResponseDTO.setAmountPaid(venta.getAmountPaid());
        ventaResponseDTO.setUserName(venta.getUsuario().getUsername());

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
