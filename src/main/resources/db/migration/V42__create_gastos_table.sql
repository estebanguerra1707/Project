CREATE TABLE gastos (
    id              BIGSERIAL PRIMARY KEY,
    descripcion     VARCHAR(255) NOT NULL,
    monto           NUMERIC(18, 2) NOT NULL,
    fecha_gasto     DATE NOT NULL,
    sucursal_id     BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_gasto_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

CREATE INDEX idx_gastos_fecha ON gastos(fecha_gasto);
CREATE INDEX idx_gastos_sucursal ON gastos(sucursal_id);