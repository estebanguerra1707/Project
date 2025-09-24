-- =========================
--  V2 - SEED CATALOGOS (IDEMPOTENTE, SEGURO)
--  - Inserta catálogos base sin dependencias complejas
--  - Ajusta secuencias de forma DINÁMICA (sin números hardcode)
-- =========================

-- Recomendado: ejecutar dentro de una transacción (Flyway ya lo hace)
-- BEGIN;

-----------------------------
-- business_type (id, nombre, activo, codigo)
-----------------------------
INSERT INTO public.business_type (id, nombre, activo, codigo) VALUES
  (7, 'Refaccionaria', true, 'REFACCIONARIA'),
  (1, 'Papeleria',     true, 'PAPELERIA'),
  (2, 'Abarrotes',     true, 'ABARROTES'),
  (3, 'Ferreteria',    true, 'FERRETERIA'),
  (8, 'Farmacia',      true, 'FARMACIA')
ON CONFLICT (id) DO NOTHING;

-----------------------------
-- product_category (id, nombre, business_type_id)
-- (ejemplos; agrega aquí todas tus categorías “base”)
-----------------------------
INSERT INTO public.product_category (id, nombre, business_type_id) VALUES
  (1,  'Cuadernos',   1),
  (2,  'Plumas',      1),
  (27, 'Analgesicos', 8)
ON CONFLICT (id) DO NOTHING;

-- Puedes agregar aquí otros catálogos “seguros”:
--  unidad_medida, payment_method, principio_activo, forma_farmaceutica, etc.
--  siempre con ON CONFLICT (id) DO NOTHING y sin depender de llaves foráneas aún.

---------------------------------------------------------
-- AJUSTE DE SECUENCIAS (DINÁMICO, SÓLO SI LA SECUENCIA EXISTE)
-- - Busca la secuencia en pg_class.
-- - Deriva el nombre de la tabla: <tabla>_id_seq -> <tabla>.
-- - Hace setval al MAX(id) de cada tabla.
---------------------------------------------------------
DO $$
DECLARE
  seq_name text;
  seq_qualified text;
  tbl_name text;
  next_sql text;
BEGIN
  -- Lista de posibles secuencias (agrega/quita según tu esquema)
  FOR seq_name IN
    SELECT unnest(ARRAY[
      'business_type_id_seq',
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
      'product_category_id_seq',
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
    -- ¿Existe la secuencia en schema public?
    IF EXISTS (
      SELECT 1
      FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE n.nspname = 'public'
        AND c.relkind = 'S'
        AND c.relname = seq_name
    ) THEN
      seq_qualified := format('public.%I', seq_name);
      -- Derivar nombre de tabla: <tabla>_id_seq -> <tabla>
      tbl_name := replace(seq_name, '_id_seq', '');

      -- Ajustar setval al MAX(id) de la tabla si existe la tabla y columna id
      IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name   = tbl_name
          AND column_name  = 'id'
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

-- COMMIT;
