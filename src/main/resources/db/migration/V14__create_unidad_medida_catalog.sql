/* =========================================================
   V14__create_unidad_medida_catalog.sql (ROBUSTO)
   - Soporta que unidad_medida ya exista con columna "unidad" NOT NULL
   - Soporta que producto.unidad_medida exista o no exista
   - Evita errores si constraints/index ya existen
   ========================================================= */

-- 0) Crear tabla si no existe (mínimo viable)
CREATE TABLE IF NOT EXISTS unidad_medida (
  id BIGSERIAL PRIMARY KEY
);

-- 0.1) Asegurar columnas (agrega las nuevas si faltan)
ALTER TABLE unidad_medida ADD COLUMN IF NOT EXISTS codigo VARCHAR(30);
ALTER TABLE unidad_medida ADD COLUMN IF NOT EXISTS unidad VARCHAR(30); -- <- tu caso
ALTER TABLE unidad_medida ADD COLUMN IF NOT EXISTS nombre VARCHAR(80);
ALTER TABLE unidad_medida ADD COLUMN IF NOT EXISTS abreviatura VARCHAR(20);
ALTER TABLE unidad_medida ADD COLUMN IF NOT EXISTS permite_decimales BOOLEAN;
ALTER TABLE unidad_medida ADD COLUMN IF NOT EXISTS active BOOLEAN;

-- 0.2) Defaults/backfill para columnas opcionales
UPDATE unidad_medida SET permite_decimales = false WHERE permite_decimales IS NULL;
UPDATE unidad_medida SET active = true WHERE active IS NULL;

-- 0.3) Asegurar que "unidad" no quede NULL si existe esa columna
--     (para no romper el NOT NULL que ya tienes)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema='public'
      AND table_name='unidad_medida'
      AND column_name='unidad'
  ) THEN
    -- si unidad es NULL pero codigo tiene algo, pásalo
    UPDATE unidad_medida
    SET unidad = codigo
    WHERE unidad IS NULL
      AND codigo IS NOT NULL;

    -- si unidad es NULL pero nombre tiene algo, derive algo
    UPDATE unidad_medida
    SET unidad = upper(regexp_replace(nombre, '\s+', '_', 'g'))
    WHERE unidad IS NULL
      AND nombre IS NOT NULL;
  END IF;
END $$;

-- 0.4) Crear UNIQUE INDEX según la columna "natural" que exista
--      Preferimos "unidad" si existe; si no, usamos "codigo"
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema='public'
      AND table_name='unidad_medida'
      AND column_name='unidad'
  ) THEN
    EXECUTE 'CREATE UNIQUE INDEX IF NOT EXISTS ux_unidad_medida_unidad ON unidad_medida(unidad)';
  ELSE
    EXECUTE 'CREATE UNIQUE INDEX IF NOT EXISTS ux_unidad_medida_codigo ON unidad_medida(codigo)';
  END IF;
END $$;

-- 0.5) Semillas (insert adaptable)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema='public'
      AND table_name='unidad_medida'
      AND column_name='unidad'
  ) THEN
    -- TU CASO: existe "unidad" (NOT NULL)
    INSERT INTO unidad_medida (unidad, codigo, nombre, abreviatura, permite_decimales, active)
    VALUES
      ('PIEZA', 'PIEZA', 'Pieza', 'pz', false, true),
      ('KG', 'KG', 'Kilogramo', 'kg', true, true),
      ('LITRO', 'LITRO', 'Litro', 'L', true, true)
      ('METRO', 'METRO', 'Metro', 'm', true, true)
    ON CONFLICT (unidad) DO NOTHING;
  ELSE
    -- Caso estándar: no existe "unidad", se usa "codigo"
    INSERT INTO unidad_medida (codigo, nombre, abreviatura, permite_decimales, active)
    VALUES
      ('PIEZA', 'Pieza', 'pz', false, true),
      ('KG', 'Kilogramo', 'kg', true, true),
      ('LITRO', 'Litro', 'L', true, true)
      ('METRO', 'METRO', 'Metro', 'm', true, true)
    ON CONFLICT (codigo) DO NOTHING;
  END IF;
END $$;

-- 1) nueva columna FK en producto
ALTER TABLE producto
ADD COLUMN IF NOT EXISTS unidad_medida_id BIGINT;

-- 2) Migración desde producto.unidad_medida (si existe)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'producto'
      AND column_name = 'unidad_medida'
  ) THEN

    -- 2.1) Insertar unidades faltantes basadas en texto viejo
    IF EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema='public'
        AND table_name='unidad_medida'
        AND column_name='unidad'
    ) THEN
      -- Si la columna natural es "unidad"
      INSERT INTO unidad_medida (unidad, codigo, nombre, abreviatura, permite_decimales, active)
      SELECT DISTINCT
             p.unidad_medida AS unidad,
             p.unidad_medida AS codigo,
             initcap(lower(p.unidad_medida)) AS nombre,
             NULL AS abreviatura,
             false AS permite_decimales,
             true AS active
      FROM producto p
      LEFT JOIN unidad_medida um ON um.unidad = p.unidad_medida
      WHERE p.unidad_medida IS NOT NULL
        AND um.id IS NULL;

      -- 2.2) Mapear a FK
      UPDATE producto p
      SET unidad_medida_id = um.id
      FROM unidad_medida um
      WHERE p.unidad_medida IS NOT NULL
        AND um.unidad = p.unidad_medida
        AND p.unidad_medida_id IS NULL;

    ELSE
      -- Caso estándar: se usa codigo
      INSERT INTO unidad_medida (codigo, nombre, abreviatura, permite_decimales, active)
      SELECT DISTINCT
             p.unidad_medida AS codigo,
             initcap(lower(p.unidad_medida)) AS nombre,
             NULL AS abreviatura,
             false AS permite_decimales,
             true AS active
      FROM producto p
      LEFT JOIN unidad_medida um ON um.codigo = p.unidad_medida
      WHERE p.unidad_medida IS NOT NULL
        AND um.id IS NULL;

      UPDATE producto p
      SET unidad_medida_id = um.id
      FROM unidad_medida um
      WHERE p.unidad_medida IS NOT NULL
        AND um.codigo = p.unidad_medida
        AND p.unidad_medida_id IS NULL;
    END IF;

    -- 2.3) Eliminar columna vieja
    ALTER TABLE producto DROP COLUMN IF EXISTS unidad_medida;

  END IF;
END $$;

-- 3) NOT NULL en producto.unidad_medida_id solo si ya no quedan NULLs
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'producto'
      AND column_name = 'unidad_medida_id'
  )
  AND NOT EXISTS (
    SELECT 1 FROM producto WHERE unidad_medida_id IS NULL
  ) THEN
    ALTER TABLE producto
    ALTER COLUMN unidad_medida_id SET NOT NULL;
  END IF;
END $$;

-- 4) FK solo si no existe
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_producto_unidad_medida'
  ) THEN
    ALTER TABLE producto
    ADD CONSTRAINT fk_producto_unidad_medida
    FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida(id);
  END IF;
END $$;

-- 5) opcional: borrar enum viejo si aplica
-- DROP TYPE IF EXISTS unidad_medida_enum;
