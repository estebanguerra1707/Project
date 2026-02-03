-- 1) Crear tabla puente Cliente <-> Sucursal
CREATE TABLE IF NOT EXISTS cliente_sucursal (
    id           BIGSERIAL PRIMARY KEY,
    cliente_id   BIGINT NOT NULL,
    sucursal_id  BIGINT NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2) Quitar constraints por si el archivo se llegó a ejecutar parcialmente (seguro para reintentos)
ALTER TABLE cliente_sucursal
    DROP CONSTRAINT IF EXISTS uk_cliente_sucursal;

ALTER TABLE cliente_sucursal
    DROP CONSTRAINT IF EXISTS fk_cliente_sucursal_cliente;

ALTER TABLE cliente_sucursal
    DROP CONSTRAINT IF EXISTS fk_cliente_sucursal_sucursal;

-- 3) Crear constraints correctos
ALTER TABLE cliente_sucursal
    ADD CONSTRAINT uk_cliente_sucursal UNIQUE (cliente_id, sucursal_id);

ALTER TABLE cliente_sucursal
    ADD CONSTRAINT fk_cliente_sucursal_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id);

ALTER TABLE cliente_sucursal
    ADD CONSTRAINT fk_cliente_sucursal_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id);

-- 4) Índices (opcionales pero recomendados)
CREATE INDEX IF NOT EXISTS idx_cliente_sucursal_sucursal_active
    ON cliente_sucursal (sucursal_id, active);

CREATE INDEX IF NOT EXISTS idx_cliente_sucursal_cliente_active
    ON cliente_sucursal (cliente_id, active);

-- 5) Backfill (opcional): si antes cliente tenía branch_id o sucursal_id, migrar a cliente_sucursal
DO $$
BEGIN
    -- Si existe cliente.branch_id
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'cliente'
          AND column_name = 'branch_id'
    ) THEN
        INSERT INTO cliente_sucursal (cliente_id, sucursal_id, active)
        SELECT c.id, c.branch_id, COALESCE(c.active, TRUE)
        FROM cliente c
        WHERE c.branch_id IS NOT NULL
        ON CONFLICT (cliente_id, sucursal_id) DO NOTHING;
    END IF;
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'cliente'
          AND column_name = 'sucursal_id'
    ) THEN
        INSERT INTO cliente_sucursal (cliente_id, sucursal_id, active)
        SELECT c.id, c.sucursal_id, COALESCE(c.active, TRUE)
        FROM cliente c
        WHERE c.sucursal_id IS NOT NULL
        ON CONFLICT (cliente_id, sucursal_id) DO NOTHING;
    END IF;
END $$;
