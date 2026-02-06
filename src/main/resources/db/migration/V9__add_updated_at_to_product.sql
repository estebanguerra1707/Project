-- 1) Agregar columna (nullable al inicio)
ALTER TABLE producto
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT (timezone('utc', now()));

UPDATE producto
SET updated_at = COALESCE(creation_date, timezone('utc', now()))
WHERE updated_at IS NULL;