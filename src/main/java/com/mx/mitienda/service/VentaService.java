package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleVentaRequest;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.model.dto.VentaRequest;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.repository.DetalleVentaRepository;
import com.mx.mitienda.repository.ProductoRepository;
import com.mx.mitienda.repository.VentaRepository;
import com.mx.mitienda.util.VentaSpecBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;


    @Transactional //si falla o hay unea excepcion en cualquier lugar del metodo, se hace rollback de todo
    public Venta registerSell(VentaRequest request, String username) {
        // Obtener cliente
        Cliente cliente = clienteRepository.findById(request.getClientId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        // Obtener usuario desde el username
        Usuario usuario = usuarioService.getByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Venta venta = new Venta();
        venta.setSale_date(LocalDate.now());
        venta.setCustomer_id(cliente);
        venta.setUsuario(usuario);
        venta.setActive(true);
        venta.setTotal_amount(BigDecimal.ZERO); // se recalcula abajo

        venta = ventaRepository.save(venta); // guardar primero para asignar en detalles

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequest d : request.getDetatails()) {
            Producto producto = productoRepository.findById(d.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

            if (producto.getStock_quantity() < d.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getName());
            }

            // Descontar stock
            producto.setStock_quantity(producto.getStock_quantity() - d.getQuantity());
            productoRepository.save(producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setSale_id(venta);
            detalle.setProduct_id(producto);
            detalle.setQuantity(d.getQuantity());
            detalle.setUnit_price(d.getPrice().doubleValue());
            detalle.setActive(true);
            detalleVentaRepository.save(detalle);

            total = total.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
        }

        venta.setTotal_amount(total);
        return ventaRepository.save(venta);
    }

    public List<Venta> getAll(String username, String rol) {
        if (rol.equals(Rol.ADMIN)) {
            return ventaRepository.findByActiveTrue();
        } else {
            return ventaRepository.findByUsernameAndActiveTrue(username);
        }
    }

    public List<Venta> findByFilter(VentaFiltroDTO filterDTO, String username, String role) {
        VentaSpecBuilder builder = new VentaSpecBuilder()
                .client(filterDTO.getClient())
                .dateBetween(filterDTO.getStart(), filterDTO.getEnd())
                .totalMajorTo(filterDTO.getMin())
                .totalMinorTo(filterDTO.getMax())
                .exactTotal(filterDTO.getTotal())
                .active(filterDTO.getActive())
                .sellPerDay(filterDTO.getDay())
                .sellPerMonth(filterDTO.getMonth())
                .sellPerYear(filterDTO.getYear())
                .withId(filterDTO.getId());
        if (!role.equals(Rol.ADMIN)) {
            builder.userName(username);
        }
        Specification<Venta> spec = builder.build();
        return ventaRepository.findAll(spec);

    }

    public List<DetalleVenta> getDetailsPerSale(Long id) {
        return detalleVentaRepository.findBySellIdAndActiveTrue(id);
    }

    public Venta getById(Long id) {
        return ventaRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
    }

    public void deleteSell(Long id) {
        Venta venta = getById(id);
        venta.setActive(false);
        ventaRepository.save(venta);

        List<DetalleVenta> detalles = detalleVentaRepository.findBySellIdAndActiveTrue(id);
        for (DetalleVenta d : detalles) {
            d.setActive(false);
            detalleVentaRepository.save(d);
        }
    }

}
