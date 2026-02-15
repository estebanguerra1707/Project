ALTER TABLE public.producto
  ALTER COLUMN unidad_medida TYPE varchar(30)
  USING unidad_medida::text;
