ALTER TABLE detalle_compra
ADD COLUMN IF NOT EXISTS owner_type VARCHAR(20);

-- 2️⃣ Default para nuevas filas
ALTER TABLE detalle_compra
ALTER COLUMN owner_type SET DEFAULT 'PROPIO';

-- 3️⃣ Backfill por seguridad (no pisa datos existentes)
UPDATE detalle_compra
SET owner_type = 'PROPIO'
WHERE owner_type IS NULL;


ALTER TABLE detalle_venta
ADD COLUMN IF NOT EXISTS owner_type VARCHAR(20);

ALTER TABLE detalle_venta
ALTER COLUMN owner_type SET DEFAULT 'PROPIO';

UPDATE detalle_venta
SET owner_type = 'PROPIO'
WHERE owner_type IS NULL;

ALTER TABLE inventario_sucursal
ADD COLUMN IF NOT EXISTS owner_type VARCHAR(20);

ALTER TABLE inventario_sucursal
ALTER COLUMN owner_type SET DEFAULT 'PROPIO';

UPDATE inventario_sucursal
SET owner_type = 'PROPIO'
WHERE owner_type IS NULL;

ALTER TABLE inventario_sucursal
ALTER COLUMN owner_type SET NOT NULL;

ALTER TABLE sucursal
ADD COLUMN IF NOT EXISTS usa_inventario_por_duenio BOOLEAN;

ALTER TABLE sucursal
ALTER COLUMN usa_inventario_por_duenio SET DEFAULT FALSE;

UPDATE sucursal
SET usa_inventario_por_duenio = FALSE
WHERE usa_inventario_por_duenio IS NULL;

ALTER TABLE sucursal
ALTER COLUMN usa_inventario_por_duenio SET NOT NULL;

CREATE INDEX idx_inventario_owner_type
ON inventario_sucursal (owner_type);
