    -- 1️⃣ Asegurar owner_type en inventario
    UPDATE inventario_sucursal
    SET owner_type = 'PROPIO'
    WHERE owner_type IS NULL;

    ALTER TABLE inventario_sucursal
    ALTER COLUMN owner_type SET NOT NULL;

    -- 2️⃣ Sucursal: inventario por dueño
    ALTER TABLE sucursal
    ADD COLUMN IF NOT EXISTS usa_inventario_por_duenio BOOLEAN;

    UPDATE sucursal
    SET usa_inventario_por_duenio = FALSE
    WHERE usa_inventario_por_duenio IS NULL;
    ALTER TABLE sucursal
    ALTER COLUMN usa_inventario_por_duenio SET NOT NULL;
a
    -- 3️⃣ Quitar constraint viejo ANTES
    ALTER TABLE inventario_sucursal
    DROP CONSTRAINT IF EXISTS uc_sucursal_producto;ac

    -- 4️⃣ Crear el nuevo constraint correcto
    ALTER TABLE inventario_sucursal
    ADD CONSTRAINT uc_sucursal_producto_owner
    UNIQUE (branch_id, product_id, owner_type);

    -- 5️⃣ Índice auxiliar (opcional pero bien)
    CREATE INDEX IF NOT EXISTS idx_inventario_owner_type
    ON inventario_sucursal (owner_type);