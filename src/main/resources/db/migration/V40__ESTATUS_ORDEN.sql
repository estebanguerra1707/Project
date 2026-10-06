-- V_X__Crear_tabla_estado_orden.sql
CREATE TABLE estado_orden (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

INSERT INTO estado_orden (nombre, descripcion) VALUES
('EN_PREPARACION', 'La orden está en cocina'),
('ENTREGADO', 'La orden fue entregada al cliente'),
('CANCELADO', 'La orden fue cancelada');

ALTER TABLE venta ADD COLUMN estado_orden_id BIGINT;
ALTER TABLE venta ADD CONSTRAINT fk_venta_estado_orden FOREIGN KEY (estado_orden_id) REFERENCES estado_orden(id);