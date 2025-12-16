-- ==============================================================
-- V3__indexes_constraints.sql
-- Índices y constraints adicionales para rendimiento
-- ==============================================================

SET search_path TO public;

-- ==============================================================
-- 1. ÍNDICES PARA PRODUCTO
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_producto_name
    ON producto (LOWER(name));

CREATE INDEX IF NOT EXISTS idx_producto_sku
    ON producto (sku);

CREATE INDEX IF NOT EXISTS idx_producto_codigo_barras_unique
    ON producto (codigo_barras);

CREATE INDEX IF NOT EXISTS idx_producto_branch
    ON producto (branch_id);

CREATE INDEX IF NOT EXISTS idx_producto_business_type
    ON producto (business_type_id);


-- ==============================================================
-- 2. ÍNDICES PARA PRODUCT_CATEGORY (búsqueda por tipo de negocio)
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_category_business_type
    ON product_category (business_type_id);


-- ==============================================================
-- 3. ÍNDICES PARA INVENTARIO
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_inv_sucursal_producto
    ON inventario_sucursal (branch_id, product_id);

CREATE INDEX IF NOT EXISTS idx_inv_stock_critico
    ON inventario_sucursal (stock_critico);


-- ==============================================================
-- 4. ÍNDICES PARA COMPRAS Y VENTAS POR FECHA
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_compra_fecha
    ON compra (purchase_date);

CREATE INDEX IF NOT EXISTS idx_venta_fecha
    ON venta (sale_date);


-- ==============================================================
-- 5. ÍNDICES PARA DETALLES (JOIN rápidos)
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_detalle_compra_precio
    ON detalle_compra (unit_price);

CREATE INDEX IF NOT EXISTS idx_detalle_venta_precio
    ON detalle_venta (unit_price);


-- ==============================================================
-- 6. HISTORIAL DE MOVIMIENTOS
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_historial_reference
    ON historial_movimiento (reference);


-- ==============================================================
-- 7. PASSWORD RESET
-- ==============================================================

CREATE INDEX IF NOT EXISTS idx_reset_token_expiry
    ON password_reset_token (expiry_date);

CREATE INDEX IF NOT EXISTS idx_reset_audit_email
    ON password_reset_audit (email);
