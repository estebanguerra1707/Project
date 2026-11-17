-- Índice compuesto para el listado principal y filtros por sucursal y fecha
CREATE INDEX IF NOT EXISTS idx_venta_branch_active_saledate
ON venta (branch_id, active, sale_date DESC);

-- Índices para llaves foráneas en los JOINs de ganancia y pagos
CREATE INDEX IF NOT EXISTS idx_detalle_venta_sale_id ON detalle_venta (sale_id);
CREATE INDEX IF NOT EXISTS idx_venta_pago_venta_id_active ON venta_pago (venta_id, active);
CREATE INDEX IF NOT EXISTS idx_detalle_venta_extras_detalle_id ON detalle_venta_extras (detalle_venta_id);