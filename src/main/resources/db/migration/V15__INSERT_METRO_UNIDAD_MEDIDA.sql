INSERT INTO unidad_medida (unidad, codigo, nombre, abreviatura, permite_decimales, active)
SELECT 'METRO', 'METRO', 'Metro', 'm', true, true
WHERE NOT EXISTS (
  SELECT 1 FROM unidad_medida WHERE codigo = 'METRO'
);