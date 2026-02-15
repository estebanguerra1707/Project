
ALTER TABLE public.inventario_farmacia
  ALTER COLUMN cantidad TYPE numeric(18,3) USING cantidad::numeric;
