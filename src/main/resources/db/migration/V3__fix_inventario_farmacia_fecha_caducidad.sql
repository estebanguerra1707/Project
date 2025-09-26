-- Convierte fecha_caducidad a DATE sólo si hoy es TIMESTAMP
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema='public'
      AND table_name='inventario_farmacia'
      AND column_name='fecha_caducidad'
      AND data_type IN ('timestamp without time zone','timestamp with time zone')
  ) THEN
    ALTER TABLE public.inventario_farmacia
      ALTER COLUMN fecha_caducidad TYPE date
      USING fecha_caducidad::date;
  END IF;
END $$;
