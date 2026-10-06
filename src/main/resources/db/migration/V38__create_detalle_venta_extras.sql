CREATE TABLE detalle_venta_extras (
    id BIGSERIAL PRIMARY KEY,
    detalle_venta_id BIGINT NOT NULL,
    product_extra_id BIGINT NOT NULL,
    extra_price DECIMAL(18, 3),
    CONSTRAINT fk_extra_detalle FOREIGN KEY (detalle_venta_id) REFERENCES detalle_venta(id),
    CONSTRAINT fk_extra_producto FOREIGN KEY (product_extra_id) REFERENCES producto(id)
);