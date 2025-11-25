package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleVentaRequestDTO;
import com.mx.mitienda.model.dto.DetalleVentaResponseDTO;
import com.mx.mitienda.model.dto.VentaRequestDTO;
import com.mx.mitienda.model.dto.VentaResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.IAuthenticatedUserService;
import com.mx.mitienda.service.IHistorialMovimientosService;
import com.mx.mitienda.service.UsuarioService;
import com.mx.mitienda.util.NumberToWordsConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.enums.TipoPago.EFECTIVO;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentasMapper {
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final SucursalRepository sucursalRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final IAuthenticatedUserService authenticatedUserService;
    private final BusinessTypeRepository businessTypeRepository;


    public Venta toEntity(VentaRequestDTO ventasRequestDTO, String username) {

        boolean isSuperAdmin = authenticatedUserService.isSuperAdmin();
        // 🔹 Si es SUPER_ADMIN → usa los valores que vienen en el request
        // 🔹 Si no lo es → toma los del usuario autenticado
        Long branchId = isSuperAdmin
                ? ventasRequestDTO.getBranchId()
                : authenticatedUserService.getCurrentBranchId();
        Long businessTypeId = isSuperAdmin
                ? sucursalRepository.findByIdAndActiveTrue(branchId)
                .map(s -> s.getBusinessType().getId())
                .orElseThrow(() -> new NotFoundException("La sucursal no tiene tipo de negocio asignado"))
                : authenticatedUserService.getCurrentBusinessTypeId();
        Cliente cliente;

        if (ventasRequestDTO.getClientId() != null && ventasRequestDTO.getClientId() > 0) {
            cliente = clienteRepository.findByIdAndActiveTrue(ventasRequestDTO.getClientId())
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        } else {
            throw new IllegalArgumentException("Debe seleccionar un cliente");
        }

        Usuario usuario = usuarioService.getByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        MetodoPago paymentMethod = metodoPagoRepository.findById(ventasRequestDTO.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado"));


        Venta venta = new Venta();
        venta.setSaleDate(ventasRequestDTO.getSaleDate());
        venta.setClient(cliente);
        venta.setUsuario(usuario);
        venta.setBranch(sucursal);
        venta.setActive(true);
        venta.setPaymentMethod(paymentMethod);

        BigDecimal amountPaid = ventasRequestDTO.getAmountPaid();
        if (EFECTIVO.name().equalsIgnoreCase(paymentMethod.getName()) &&
                (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) == 0)) {
            throw new IllegalArgumentException("La cantidad a pagar no puede estar en ceros para pagos en "+ EFECTIVO.name());
        }else{
            venta.setAmountPaid(amountPaid);
        }

        List<DetalleVenta> details = ventasRequestDTO.getDetails().stream().map(detalleVentaRequestDTO -> {
            Producto producto = productoRepository.findById(detalleVentaRequestDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

            Long tipoProducto = producto.getProductCategory().getBusinessType().getId();
            if (!tipoProducto.equals(businessTypeId)) {
                throw new NotFoundException(String.format(
                        "El producto '%s' no pertenece al tipo de negocio '%s' de la sucursal '%s'",
                        producto.getName(),
                        businessTypeRepository.findByIdAndActiveTrue(businessTypeId).map(BusinessType::getName),
                        sucursal.getName()
                ));
            }

            Integer cantidad = detalleVentaRequestDTO.getQuantity();
            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para producto: " + producto.getName());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProduct(producto);
            detalle.setQuantity(cantidad);
            detalle.setUnitPrice(producto.getSalePrice());
            detalle.setSubTotal(producto.getSalePrice().multiply(BigDecimal.valueOf(cantidad)));
            detalle.setActive(true);
            return detalle;
        }).collect(Collectors.toList());

        venta.setDetailsList(details);

        BigDecimal total = details.stream()
                .map(DetalleVenta::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        venta.setTotalAmount(total);

        if (EFECTIVO.name().equalsIgnoreCase(paymentMethod.getName())) {
            if (amountPaid.compareTo(total) < 0) {
                throw new IllegalArgumentException("El monto pagado no puede ser menor al total.");
            }
            venta.setChangeAmount(amountPaid.subtract(total));
        } else {
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
        ventaResponseDTO.setActive(venta.getActive());

        List<DetalleVentaResponseDTO> details = venta.getDetailsList().stream().map(detail->{
            DetalleVentaResponseDTO detalleVentaResponseDTO = new DetalleVentaResponseDTO();
            detalleVentaResponseDTO.setProductId(detail.getProduct().getId());
            detalleVentaResponseDTO.setSku(detail.getProduct().getSku());
            detalleVentaResponseDTO.setBranchId(detail.getProduct().getBranch().getId());
            detalleVentaResponseDTO.setBranchName(detail.getProduct().getBranch().getName());
            detalleVentaResponseDTO.setBusinessTypeId(detail.getProduct().getBusinessType().getId());
            detalleVentaResponseDTO.setBusinessTypeName(detail.getProduct().getBusinessType().getName());
            detalleVentaResponseDTO.setCodigoBarras(detail.getProduct().getCodigoBarras());
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
