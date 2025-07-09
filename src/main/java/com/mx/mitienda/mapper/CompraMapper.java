package com.mx.mitienda.mapper;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.CompraRequestDTO;
import com.mx.mitienda.model.dto.CompraResponseDTO;
import com.mx.mitienda.model.dto.DetalleCompraResponseDTO;
import com.mx.mitienda.repository.*;
import com.mx.mitienda.service.UsuarioService;
import com.mx.mitienda.util.NumberToWordsConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.mx.mitienda.util.enums.TipoPago.EFECTIVO;

@Component
@RequiredArgsConstructor
public class CompraMapper {

    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public Compra toEntity(CompraRequestDTO compraRequestDTO, String userName){

        Compra compra = new Compra();
        Usuario usuario  = usuarioRepository.findByUsernameAndActiveTrue(userName).orElseThrow(()->new NotFoundException("Usuario no encontrado: " + userName));
        Proveedor proveedor = proveedorRepository.findByIdAndActiveTrue(compraRequestDTO.getProviderId()).orElseThrow(()-> new NotFoundException("Proveedor no alineado con el producto, favor de verificar"));
        Sucursal sucursal = sucursalRepository.findByIdAndActiveTrue(compraRequestDTO.getBranchId()).orElseThrow(()->new NotFoundException("No se ha podido encontrar la sucursal, favor de verificarla"));
        MetodoPago paymentMethod = metodoPagoRepository.findById(compraRequestDTO.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado"));
        compra.setProveedor(proveedor);
        compra.setBranch(sucursal);
        compra.setUsuario(usuario);
        compra.setActive(true);
        compra.setPurchaseDate(compraRequestDTO.getPurchaseDate());
        compra.setPaymentMethod(paymentMethod);
        if(compra.getPaymentMethod().getName().equalsIgnoreCase(EFECTIVO.name().toLowerCase())){
            compra.setAmountPaid(compraRequestDTO.getAmountPaid());
        }else{
            compra.setAmountPaid(BigDecimal.ZERO);
        }
        if (compra.getAmountPaid()==null) {
            throw new IllegalArgumentException("Debes indicar el monto pagado");
        }

        List<DetalleCompra> details = compraRequestDTO.getDetails().stream().map(detalleDTO->{
            Producto producto = productoRepository.findByIdAndActiveTrue(detalleDTO.getProductId())
                    .orElseThrow(()-> new NotFoundException("Producto no encontrado"));

            DetalleCompra detail = new DetalleCompra();
            detail.setQuantity(detalleDTO.getQuantity());
            BigDecimal unitPrice = producto.getPurchasePrice();
            detail.setCompra(compra);
            detail.setUnitPrice(unitPrice);

            BigDecimal quantity = BigDecimal.valueOf(detalleDTO.getQuantity());
            BigDecimal subtotal = unitPrice.multiply(quantity);
            detail.setSubTotal(subtotal);
            detail.setActive(true);
            InventarioSucursal inventarioSucursal = inventarioSucursalRepository.findByProduct_IdAndBranch_Id(producto.getId(), sucursal.getId()).orElse(new InventarioSucursal());
            inventarioSucursal.setProduct(producto);
            inventarioSucursal.setBranch(sucursal);
            inventarioSucursal.setQuantity(
                    (inventarioSucursal.getQuantity()!=null ? inventarioSucursal.getQuantity():0) + detail.getQuantity()
            );

            inventarioSucursalRepository.save(inventarioSucursal);
            detail.setProduct(producto);
            return detail;
        }).toList();
       compra.setDetails(details);
        BigDecimal total = details.stream()
                .map(DetalleCompra::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setTotalAmount(total);
        if (compra.getPaymentMethod().getName().equalsIgnoreCase(EFECTIVO.name().toLowerCase())) {
            if (compra.getAmountPaid().compareTo(total) < 0) {
                throw new IllegalArgumentException("El monto pagado no puede ser menor al total.");
            }
            compra.setChangeAmount(compra.getAmountPaid().subtract(total));
        } else {
            compra.setChangeAmount(BigDecimal.ZERO);
        }

        return compra;
    }

    public CompraResponseDTO toResponse(Compra compra) {
        CompraResponseDTO compraResponseDTO = new CompraResponseDTO();
        compraResponseDTO.setId(compra.getId());
        compraResponseDTO.setTotalAmount(compra.getTotalAmount());
        compraResponseDTO.setProviderName(compra.getProveedor().getName());
        compraResponseDTO.setPurchaseDate(compra.getPurchaseDate());
        compraResponseDTO.setPaymentMethodId(compra.getPaymentMethod().getId());
        compraResponseDTO.setPaymentName(compra.getPaymentMethod().getName());
        compraResponseDTO.setAmountPaid(compra.getAmountPaid());
        compraResponseDTO.setChangeAmount(compra.getChangeAmount());
        compraResponseDTO.setUserId(compra.getUsuario().getId());
        compraResponseDTO.setUserName(compra.getUsuario().getUsername());
        compraResponseDTO.setAmountInWords(NumberToWordsConverter.convert(compra.getTotalAmount()));
        List<DetalleCompraResponseDTO> details = compra.getDetails().stream().map(detail->{
            DetalleCompraResponseDTO detalleCompraResponseDTO = new DetalleCompraResponseDTO();
            detalleCompraResponseDTO.setProductName(detail.getProduct().getName());
            detalleCompraResponseDTO.setQuantity(detail.getQuantity());
            detalleCompraResponseDTO.setUnitPrice(detail.getUnitPrice());
            detalleCompraResponseDTO.setSubTotal(detail.getSubTotal());
            return detalleCompraResponseDTO;
        }).toList();
    compraResponseDTO.setDetails(details);
    return compraResponseDTO;
    }
}
