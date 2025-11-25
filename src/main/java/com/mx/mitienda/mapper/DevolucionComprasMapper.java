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
            @Mapping(target = "fechaDevolucion", source = "fecha"), // o "fecha" si así se llama tu campo
            @Mapping(target = "tipoDevolucion",  expression = "java( devolucion.getTipoDevolucion()!=null ? devolucion.getTipoDevolucion().name() : null )"),
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

    // interface DevolucionComprasMapper (MapStruct)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "compra", source = "compra"),
            @Mapping(target = "usuario", source = "usuario"),
            @Mapping(target = "branch", source = "branch"),

            // Fecha del sistema
            @Mapping(target = "fecha", expression = "java(LocalDateTime.now())"),

            @Mapping(target = "motivo", source = "request.motivo"),

            // TOTAL o PARCIAL según la cantidad devuelta
            @Mapping(
                    target = "tipoDevolucion",
                    expression =
                            "java(request.getCantidad() != null && " +
                                    "detalleCompra.getQuantity().equals(request.getCantidad()) ? " +
                                    "com.mx.mitienda.util.enums.TipoDevolucion.TOTAL : " +
                                    "com.mx.mitienda.util.enums.TipoDevolucion.PARCIAL)"
            ),

            // Monto devuelto = unitPrice * cantidad devuelta
            @Mapping(
                    target = "montoDevuelto",
                    expression =
                            "java(detalleCompra.getUnitPrice()" +
                                    ".multiply(java.math.BigDecimal.valueOf(request.getCantidad())))"
            ),

            // Se llena en el @AfterMapping
            @Mapping(target = "detalles", ignore = true)
    })
    DevolucionCompras toEntity(DevolucionComprasRequestDTO request,
                               Compra compra,
                               DetalleCompra detalleCompra,
                               Usuario usuario,
                               Sucursal branch);

    @AfterMapping
    default void fillDetalles(@MappingTarget DevolucionCompras devolucionCompras,
                              DevolucionComprasRequestDTO devolucionComprasRequestDTO,
                              DetalleCompra detalleCompra) {
        DetalleDevolucionCompras detalleDevolucionCompras = new DetalleDevolucionCompras();
        detalleDevolucionCompras.setDetalleCompra(detalleCompra);
        detalleDevolucionCompras.setCantidadDevuelta(devolucionComprasRequestDTO.getCantidad());
        detalleDevolucionCompras.setProducto(detalleCompra.getProduct());
        // usa el nombre real en tu entidad de detalle (precioCompra o precioUnitario)
        detalleDevolucionCompras.setPrecioCompra(detalleCompra.getUnitPrice());
        detalleDevolucionCompras.setDevolucion(devolucionCompras);
        devolucionCompras.setDetalles(java.util.List.of(detalleDevolucionCompras));
    }
}
