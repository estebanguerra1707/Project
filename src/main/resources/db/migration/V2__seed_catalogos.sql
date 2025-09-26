-- =========================
--  V2 - SEED CATALOGOS (IDEMPOTENTE Y ROBUSTO A NOMBRES DE COLUMNA)
--  Inserta business_type y product_category detectando nombres (nombre/name, activo/active, codigo/code)
-- =========================

-- BUSINESS_TYPE
DO $$
DECLARE
  col_name   text;
  col_active text;
  col_code   text;
  sql_stmt   text;
BEGIN
  -- Detecta columnas
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema='public' AND table_name='business_type' AND column_name='nombre') THEN
    col_name := 'nombre';
  ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='business_type' AND column_name='name') THEN
    col_name := 'name';
  ELSE
    RAISE EXCEPTION 'business_type: no encuentro columna nombre/name';
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema='public' AND table_name='business_type' AND column_name='activo') THEN
    col_active := 'activo';
  ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='business_type' AND column_name='active') THEN
    col_active := 'active';
  ELSE
    RAISE EXCEPTION 'business_type: no encuentro columna activo/active';
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema='public' AND table_name='business_type' AND column_name='codigo') THEN
    col_code := 'codigo';
  ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='business_type' AND column_name='code') THEN
    col_code := 'code';
  ELSE
    RAISE EXCEPTION 'business_type: no encuentro columna codigo/code';
  END IF;

  -- Inserta filas idempotentes
  sql_stmt := format($f$
    INSERT INTO public.business_type (id, %1$I, %2$I, %3$I) VALUES
      (7, 'Refaccionaria', true, 'REFACCIONARIA'),
      (1, 'Papeleria',     true, 'PAPELERIA'),
      (2, 'Abarrotes',     true, 'ABARROTES'),
      (3, 'Ferreteria',    true, 'FERRETERIA'),
      (8, 'Farmacia',      true, 'FARMACIA')
    ON CONFLICT (id) DO NOTHING;
  $f$, col_name, col_active, col_code);

  EXECUTE sql_stmt;
END $$;

-- PRODUCT_CATEGORY
DO $$
DECLARE
  col_name text;
  sql_stmt text;
BEGIN
  -- Detecta columna nombre/name
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema='public' AND table_name='product_category' AND column_name='nombre') THEN
    col_name := 'nombre';
  ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='product_category' AND column_name='name') THEN
    col_name := 'name';
  ELSE
    RAISE EXCEPTION 'product_category: no encuentro columna nombre/name';
  END IF;

  -- Inserta filas (asumimos business_type_id se llama igual)
  sql_stmt := format($f$
    INSERT INTO public.product_category (id, %1$I, business_type_id) VALUES
      (1,  'Cuadernos',   1),
      (2,  'Plumas',      1),
      (27, 'Analgesicos', 8)
    ON CONFLICT (id) DO NOTHING;
  $f$, col_name);

  EXECUTE sql_stmt;
END $$;

-- ===== AJUSTE DINÁMICO DE SECUENCIAS =====
DO $$
DECLARE
  seq_name text;
  seq_qualified text;
  tbl_name text;
  next_sql text;
BEGIN
  FOR seq_name IN
    SELECT unnest(ARRAY[
      'business_type_id_seq',
      'product_category_id_seq',
      'cliente_id_seq',
      'compra_id_seq',
      'detalle_compra_id_seq',
      'detalle_devolucion_compras_id_seq',
      'detalle_devolucion_ventas_id_seq',
      'detalle_venta_id_seq',
      'devolucion_compras_id_seq',
      'devolucion_ventas_id_seq',
      'forma_farmaceutica_id_seq',
      'historial_movimiento_id_seq',
      'inventario_farmacia_id_seq',
      'inventario_sucursal_id_seq',
      'payment_method_id_seq',
      'principio_activo_id_seq',
      'product_detail_id_seq',
      'producto_farmacia_id_seq',
      'producto_id_seq',
      'proveedor_id_seq',
      'proveedor_sucursal_id_seq',
      'sucursal_id_seq',
      'unidad_medida_id_seq',
      'usuario_id_seq',
      'venta_id_seq'
    ])
  LOOP
    IF EXISTS (
      SELECT 1
      FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE n.nspname = 'public' AND c.relkind = 'S' AND c.relname = seq_name
    ) THEN
      seq_qualified := format('public.%I', seq_name);
      tbl_name := replace(seq_name, '_id_seq', '');
      IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = tbl_name AND column_name = 'id'
      ) THEN
        next_sql := format(
          'SELECT setval(''%s'', COALESCE((SELECT MAX(id) FROM public.%I), 1), true);',
          seq_qualified, tbl_name
        );
        EXECUTE next_sql;
      END IF;
    END IF;
  END LOOP;
END $$;
