CREATE TABLE reserva_bot (
    id              BIGSERIAL PRIMARY KEY,
    cliente_id      BIGINT,
    sucursal_id     BIGINT,
    hora_inicio     TIMESTAMP NOT NULL,
    hora_fin        TIMESTAMP NOT NULL,
    estado          VARCHAR(50) NOT NULL DEFAULT 'CONFIRMADO',
    version         BIGINT DEFAULT 0, -- ¡Clave para evitar choques simultáneos!

    CONSTRAINT fk_reserva_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_reserva_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

CREATE INDEX idx_reserva_fechas ON reserva_bot(hora_inicio, hora_fin);