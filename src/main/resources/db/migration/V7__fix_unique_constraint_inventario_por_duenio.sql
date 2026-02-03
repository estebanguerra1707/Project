-- V7__fix_unique_constraint_inventario_por_duenio.sql

-- 1️⃣ Eliminar constraint incorrecto
ALTER TABLE inventario_sucursal
DROP CONSTRAINT IF EXISTS uc_sucursal_producto;

-- 2️⃣ Crear el constraint correcto por dueño
ALTER TABLE inventario_sucursal
ADD CONSTRAINT uc_sucursal_producto_owner
UNIQUE (branch_id, product_id, owner_type);

-- 3️⃣ Índice auxiliar (opcional pero recomendado)
CREATE INDEX IF NOT EXISTS idx_inventario_owner_type
ON inventario_sucursal (owner_type);
