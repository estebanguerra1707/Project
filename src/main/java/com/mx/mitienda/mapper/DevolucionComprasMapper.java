package com.mx.mitienda.mapper;

import com.mx.mitienda.model.*;
import com.mx.mitienda.model.dto.DetalleDevolucionCompraDTO;
import com.mx.mitienda.model.dto.DevolucionComprasReponseDTO;
import com.mx.mitienda.model.dto.DevolucionComprasRequestDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = {
                java.time.LocalDateTime.class,
                java.math.BigDecimal.class,
                com.mx.mitienda.util.enums.TipoDevolucion.class
        }
)
public interface DevolucionComprasMapper {

    @Mappings({
            @Mapping(target = "compraId",        source = "compra.id"),
            @Mapping(target = "fechaDevolucion", source = "fecha"),
            @Mapping(target = "tipoDevolucion",  expression = "java(devolucion.getTipoDevolucion()!=null ? devolucion.getTipoDevolucion().name() : null)"),
            @Mapping(target = "sucursal",        source = "branch.name"),
            @Mapping(target = "motivo",          source = "motivo"),
            @Mapping(target = "totalDevolucion", source = "montoDevuelto"),
            @Mapping(target = "detalles",        source = "detalles"),
            @Mapping(target = "usuario",         source = "usuario.username")
    })
    DevolucionComprasReponseDTO toResponse(DevolucionCompras devolucion);

    @Mappings({
            @Mapping(target = "productId",        source = "detalleCompra.product.id"),
            @Mapping(target = "productName",      source = "detalleCompra.product.name"),
            @Mapping(target = "precioCompra",     source = "precioCompra"),
            @Mapping(target = "cantidadDevuelta", source = "cantidadDevuelta")
    })
    DetalleDevolucionCompraDTO toDetalleResponse(DetalleDevolucionCompras detalle);

    List<DetalleDevolucionCompraDTO> toDetalleList(List<DetalleDevolucionCompras> detalles);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "compra", source = "compra"),
            @Mapping(target = "usuario", source = "usuario"),
            @Mapping(target = "branch", source = "branch"),

            @Mapping(target = "fecha", expression = "java(LocalDateTime.now())"),
            @Mapping(target = "motivo", source = "request.motivo"),

            // OJO: si en tu Service tú ya decides TOTAL/PARCIAL por acumulado,
            // puedes dejar esto como PARCIAL y sobreescribir después.
            @Mapping(
                    target = "tipoDevolucion",
                    expression =
                            "java(request.getCantidad() != null && " +
                                    "detalleCompra.getQuantity() != null && " +
                                    "detalleCompra.getQuantity().compareTo(request.getCantidad()) == 0 ? " +
                                    "com.mx.mitienda.util.enums.TipoDevolucion.TOTAL : " +
                                    "com.mx.mitienda.util.enums.TipoDevolucion.PARCIAL)"
            ),

            // Monto devuelto = unitPrice * cantidad (BigDecimal * BigDecimal)
            @Mapping(
                    target = "montoDevuelto",
                    expression =
                            "java(detalleCompra.getUnitPrice() != null && request.getCantidad() != null ? " +
                                    "detalleCompra.getUnitPrice().multiply(request.getCantidad()) : java.math.BigDecimal.ZERO)"
            ),

            @Mapping(target = "detalles", ignore = true)
    })
    DevolucionCompras toEntity(
            DevolucionComprasRequestDTO request,
            Compra compra,
            DetalleCompra detalleCompra,
            Usuario usuario,
            Sucursal branch
    );

    @AfterMapping
    default void fillDetalles(
            @MappingTarget DevolucionCompras devolucionCompras,
            DevolucionComprasRequestDTO request,
            DetalleCompra detalleCompra
    ) {
        DetalleDevolucionCompras detalle = new DetalleDevolucionCompras();
        detalle.setDetalleCompra(detalleCompra);
        detalle.setProducto(detalleCompra.getProduct());

        // cantidadDevuelta BigDecimal
        detalle.setCantidadDevuelta(request.getCantidad());

        // usa el nombre real (precioCompra o precioUnitario)
        detalle.setPrecioCompra(detalleCompra.getUnitPrice());

        detalle.setDevolucion(devolucionCompras);
        devolucionCompras.setDetalles(java.util.List.of(detalle));
    }
}
