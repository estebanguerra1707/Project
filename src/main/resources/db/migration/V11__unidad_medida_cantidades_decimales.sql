-- 1) Crear enum con nombre que NO choque
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_type t
    JOIN pg_namespace n ON n.oid = t.typnamespace
    WHERE t.typname = 'unidad_medida_enum'
      AND n.nspname = 'public'
  ) THEN
    CREATE TYPE public.unidad_medida_enum AS ENUM (
      'PIEZA',
      'KILOGRAMO',
      'GRAMO',
      'LITRO',
      'MILILITRO',
      'METRO',
      'CENTIMETRO'
    );
  END IF;
END $$;

-- 2) producto
ALTER TABLE public.producto
  ADD COLUMN IF NOT EXISTS unidad_medida public.unidad_medida_enum
    NOT NULL DEFAULT 'PIEZA'::public.unidad_medida_enum,
  ADD COLUMN IF NOT EXISTS permite_decimales boolean
    NOT NULL DEFAULT false;

-- PIEZA es la única que NO permite decimales
UPDATE public.producto
SET permite_decimales = (unidad_medida <> 'PIEZA'::public.unidad_medida_enum)
WHERE permite_decimales = false;

-- 3) inventario_sucursal: stock/min/max a decimal
ALTER TABLE public.inventario_sucursal
  ALTER COLUMN stock     TYPE numeric(18,3) USING stock::numeric,
  ALTER COLUMN min_stock TYPE numeric(18,3) USING min_stock::numeric,
  ALTER COLUMN max_stock TYPE numeric(18,3) USING max_stock::numeric;

-- 4) detalle_compra: quantity decimal
ALTER TABLE public.detalle_compra
  ALTER COLUMN quantity TYPE numeric(18,3) USING quantity::numeric;

-- 5) detalle_venta: quantity y cantidad_devuelta decimal
ALTER TABLE public.detalle_venta
  ALTER COLUMN quantity          TYPE numeric(18,3) USING quantity::numeric,
  ALTER COLUMN cantidad_devuelta TYPE numeric(18,3) USING cantidad_devuelta::numeric;

-- 6) historial_movimiento: decimal
ALTER TABLE public.historial_movimiento
  ALTER COLUMN quantity     TYPE numeric(18,3) USING quantity::numeric,
  ALTER COLUMN before_stock TYPE numeric(18,3) USING before_stock::numeric,
  ALTER COLUMN new_stock    TYPE numeric(18,3) USING new_stock::numeric;

-- 7) detalle_devolucion_compras: decimal
ALTER TABLE public.detalle_devolucion_compras
  ALTER COLUMN cantidad_devuelta TYPE numeric(18,3) USING cantidad_devuelta::numeric;

-- 8) detalle_devolucion_ventas (si existe): decimal
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name   = 'detalle_devolucion_ventas'
  ) THEN
    EXECUTE '
      ALTER TABLE public.detalle_devolucion_ventas
        ALTER COLUMN cantidad_devuelta TYPE numeric(18,3) USING cantidad_devuelta::numeric;
    ';
  END IF;
END $$;