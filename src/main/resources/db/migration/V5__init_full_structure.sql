-- ===========================================
-- V4: FULL INITIAL STRUCTURE FOR MI TIENDA
-- ===========================================

-- ================
-- SEQUENCES
-- ================
CREATE SEQUENCE IF NOT EXISTS business_type_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS category_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS product_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS product_detail_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS sucursal_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS usuario_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS proveedor_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS compra_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS venta_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS historial_mov_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS devolucion_ventas_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS detalle_devolucion_ventas_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS devolucion_compras_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS detalle_devolucion_compras_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS password_reset_token_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS inventory_farmacia_id_seq START WITH 1 INCREMENT BY 1;

-- =============================
-- BUSINESS TYPE
-- =============================
CREATE TABLE IF NOT EXISTS business_type (
    id BIGINT PRIMARY KEY DEFAULT nextval('business_type_id_seq'),
    name VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================
-- CATEGORY
-- =============================
CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT PRIMARY KEY DEFAULT nextval('category_id_seq'),
    name VARCHAR(255) NOT NULL,
    business_type_id BIGINT NOT NULL,
    CONSTRAINT fk_category_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_type(id)
);

-- =============================
-- USUARIOS Y ROLES
-- =============================
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT PRIMARY KEY DEFAULT nextval('usuario_id_seq'),
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================
-- PASSWORD RESET
-- =============================
CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT PRIMARY KEY DEFAULT nextval('password_reset_token_id_seq'),
    user_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiration TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_audit (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    accion VARCHAR(50) NOT NULL
);

-- ===========================================
-- PRODUCTOS
-- ===========================================
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT PRIMARY KEY DEFAULT nextval('product_id_seq'),
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio_compra NUMERIC(12, 2) NOT NULL,
    precio_venta NUMERIC(12, 2) NOT NULL,
    codigo_barras VARCHAR(255) UNIQUE,
    stock_global INTEGER NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    business_type_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (category_id) REFERENCES product_category(id),

    CONSTRAINT fk_producto_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_type(id)
);

-- ===========================================
-- PRODUCTO FARMACIA (estructura extendida)
-- ===========================================
CREATE TABLE IF NOT EXISTS producto_farmacia (
    id BIGINT PRIMARY KEY,
    sustancia_activa VARCHAR(255),
    forma_farmaceutica VARCHAR(255),
    concentracion VARCHAR(255),
    presentacion VARCHAR(255),
    requiere_receta BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_producto_farmacia_producto
        FOREIGN KEY (id) REFERENCES producto(id) ON DELETE CASCADE
);

-- ===========================================
-- PRODUCT DETAIL (para farmacias y otros)
-- ===========================================
CREATE TABLE IF NOT EXISTS product_detail (
    id BIGINT PRIMARY KEY DEFAULT nextval('product_detail_id_seq'),
    producto_id BIGINT NOT NULL,
    dosis VARCHAR(255),
    unidad_medida VARCHAR(100),
    fecha_caducidad DATE,
    lote VARCHAR(255),
    marca VARCHAR(255),

    CONSTRAINT fk_product_detail_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- ===========================================
-- SUCURSALES
-- ===========================================
CREATE TABLE IF NOT EXISTS sucursal (
    id BIGINT PRIMARY KEY DEFAULT nextval('sucursal_id_seq'),
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500),
    telefono VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===========================================
-- PROVEEDORES
-- ===========================================
CREATE TABLE IF NOT EXISTS proveedor (
    id BIGINT PRIMARY KEY DEFAULT nextval('proveedor_id_seq'),
    nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(50),
    correo VARCHAR(255),
    direccion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===========================================
-- ASOCIACIÓN PROVEEDOR - SUCURSAL
-- ===========================================
CREATE TABLE IF NOT EXISTS proveedor_sucursal (
    proveedor_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,

    PRIMARY KEY (proveedor_id, sucursal_id),

    CONSTRAINT fk_proveedor_sucursal_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),

    CONSTRAINT fk_proveedor_sucursal_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

-- ===========================================
-- INVENTARIO POR SUCURSAL
-- ===========================================
CREATE TABLE IF NOT EXISTS inventario_sucursal (
    id BIGINT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER DEFAULT 0,
    stock_maximo INTEGER DEFAULT 0,
    stock_critico INTEGER DEFAULT 0,
    last_updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_inventario_sucursal_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),

    CONSTRAINT fk_inventario_sucursal_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

-- ===========================================
-- INVENTARIO FARMACIA
-- ===========================================
CREATE TABLE IF NOT EXISTS inventario_farmacia (
    id BIGINT PRIMARY KEY DEFAULT nextval('inventory_farmacia_id_seq'),
    producto_farmacia_id BIGINT NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    fecha_caducidad DATE,
    lote VARCHAR(255),
    ubicacion VARCHAR(255),
    sucursal_id BIGINT NOT NULL,

    CONSTRAINT fk_inv_farmacia_producto_f
        FOREIGN KEY (producto_farmacia_id) REFERENCES producto_farmacia(id),

    CONSTRAINT fk_inv_farmacia_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);
-- ===========================================
-- COMPRAS
-- ===========================================
CREATE TABLE IF NOT EXISTS compra (
    id BIGINT PRIMARY KEY DEFAULT nextval('compra_id_seq'),
    proveedor_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    usuario_id BIGINT NOT NULL,

    CONSTRAINT fk_compra_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),

    CONSTRAINT fk_compra_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id),

    CONSTRAINT fk_compra_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ===========================================
-- DETALLE DE COMPRA
-- ===========================================
CREATE TABLE IF NOT EXISTS detalle_compra (
    id BIGINT PRIMARY KEY DEFAULT nextval('compra_id_seq'),
    compra_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,

    fecha_caducidad DATE,
    lote VARCHAR(255),

    CONSTRAINT fk_detalle_compra_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id),

    CONSTRAINT fk_detalle_compra_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- ===========================================
-- VENTAS
-- ===========================================
CREATE TABLE IF NOT EXISTS venta (
    id BIGINT PRIMARY KEY DEFAULT nextval('venta_id_seq'),
    sucursal_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    metodo_pago VARCHAR(50) NOT NULL,

    CONSTRAINT fk_venta_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id),

    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ===========================================
-- DETALLE DE VENTA
-- ===========================================
CREATE TABLE IF NOT EXISTS detalle_venta (
    id BIGINT PRIMARY KEY DEFAULT nextval('venta_id_seq'),
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,

    cantidad_devuelta INTEGER DEFAULT 0,

    CONSTRAINT fk_detalle_venta_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),

    CONSTRAINT fk_detalle_venta_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);
-- ===========================================
-- HISTORIAL DE MOVIMIENTOS
-- ===========================================
CREATE TABLE IF NOT EXISTS historial_movimientos (
    id BIGINT PRIMARY KEY DEFAULT nextval('historial_mov_id_seq'),
    producto_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,       -- ENTRADA / SALIDA
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    referencia VARCHAR(255),                    -- puede contener ID de compra o venta
    usuario_id BIGINT,

    CONSTRAINT fk_historial_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),

    CONSTRAINT fk_historial_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id),

    CONSTRAINT fk_historial_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ===========================================
-- DEVOLUCION DE VENTAS
-- ===========================================
CREATE TABLE IF NOT EXISTS devolucion_ventas (
    id BIGINT PRIMARY KEY DEFAULT nextval('devolucion_ventas_id_seq'),
    venta_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    tipo_devolucion VARCHAR(50) NOT NULL, -- PARCIAL / TOTAL
    motivo TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    monto_reintegrado NUMERIC(12, 2) DEFAULT 0,

    CONSTRAINT fk_dev_ventas_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),

    CONSTRAINT fk_dev_ventas_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),

    CONSTRAINT fk_dev_ventas_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

-- ===========================================
-- DETALLE DEVOLUCION DE VENTAS
-- ===========================================
CREATE TABLE IF NOT EXISTS detalle_devolucion_ventas (
    id BIGINT PRIMARY KEY DEFAULT nextval('detalle_devolucion_ventas_id_seq'),
    devolucion_ventas_id BIGINT NOT NULL,
    detalle_venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad_devuelta INTEGER NOT NULL,
    subtotal_devuelto NUMERIC(12, 2) DEFAULT 0,

    CONSTRAINT fk_detalle_dev_ventas_dev
        FOREIGN KEY (devolucion_ventas_id) REFERENCES devolucion_ventas(id),

    CONSTRAINT fk_detalle_dev_ventas_detalle_venta
        FOREIGN KEY (detalle_venta_id) REFERENCES detalle_venta(id),

    CONSTRAINT fk_detalle_dev_ventas_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- ===========================================
-- DEVOLUCION DE COMPRAS
-- ===========================================
CREATE TABLE IF NOT EXISTS devolucion_compras (
    id BIGINT PRIMARY KEY DEFAULT nextval('devolucion_compras_id_seq'),
    compra_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    tipo_devolucion VARCHAR(50) NOT NULL,       -- PARCIAL / TOTAL
    motivo TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    monto_ajustado NUMERIC(12, 2) DEFAULT 0,

    CONSTRAINT fk_dev_compras_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id),

    CONSTRAINT fk_dev_compras_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),

    CONSTRAINT fk_dev_compras_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

-- ===========================================
-- DETALLE DEVOLUCION DE COMPRAS
-- ===========================================
CREATE TABLE IF NOT EXISTS detalle_devolucion_compras (
    id BIGINT PRIMARY KEY DEFAULT nextval('detalle_devolucion_compras_id_seq'),
    devolucion_compras_id BIGINT NOT NULL,
    detalle_compra_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad_devuelta INTEGER NOT NULL,
    subtotal_ajustado NUMERIC(12,2) DEFAULT 0,

    CONSTRAINT fk_detalle_dev_compras_dev
        FOREIGN KEY (devolucion_compras_id) REFERENCES devolucion_compras(id),

    CONSTRAINT fk_detalle_dev_compras_detalle_compra
        FOREIGN KEY (detalle_compra_id) REFERENCES detalle_compra(id),

    CONSTRAINT fk_detalle_dev_compras_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);
-- ===========================================
-- ÍNDICES GENERALES
-- ===========================================

-- Producto
CREATE INDEX IF NOT EXISTS idx_producto_codigo_barras
    ON producto (codigo_barras);

CREATE INDEX IF NOT EXISTS idx_producto_categoria
    ON producto (category_id);

-- Inventario Sucursal
CREATE INDEX IF NOT EXISTS idx_inv_sucursal_producto
    ON inventario_sucursal (producto_id);

-- Historial de movimientos
CREATE INDEX IF NOT EXISTS idx_historial_producto
    ON historial_movimientos (producto_id);

CREATE INDEX IF NOT EXISTS idx_historial_sucursal
    ON historial_movimientos (sucursal_id);

-- Ventas
CREATE INDEX IF NOT EXISTS idx_venta_sucursal
    ON venta (sucursal_id);

-- Compras
CREATE INDEX IF NOT EXISTS idx_compra_sucursal
    ON compra (sucursal_id);

-- Devoluciones
CREATE INDEX IF NOT EXISTS idx_dev_ventas_venta
    ON devolucion_ventas (venta_id);

CREATE INDEX IF NOT EXISTS idx_dev_compras_compra
    ON devolucion_compras (compra_id);


-- ===========================================
-- RELACIONES FALTANTES Y AJUSTES IMPORTANTES
-- ===========================================

-- Asegurar que inventario_sucursal tenga PK único por producto + sucursal
ALTER TABLE inventario_sucursal
    DROP CONSTRAINT IF EXISTS inventario_sucursal_pkey;

ALTER TABLE inventario_sucursal
    ADD CONSTRAINT inventario_sucursal_pkey PRIMARY KEY (producto_id, sucursal_id);

-- Ajuste de claves duplicadas en detalle_compra y detalle_venta
ALTER TABLE detalle_compra
    DROP CONSTRAINT IF EXISTS detalle_compra_pkey;

ALTER TABLE detalle_venta
    DROP CONSTRAINT IF EXISTS detalle_venta_pkey;


-- ===========================================
-- CONSTRAINTS DE INTEGRIDAD
-- ===========================================

-- Evitar stocks negativos
ALTER TABLE inventario_sucursal
    ADD CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0);

-- Evitar cantidades devueltas negativas
ALTER TABLE detalle_devolucion_ventas
    ADD CONSTRAINT chk_dev_ventas_no_negativo CHECK (cantidad_devuelta >= 0);

ALTER TABLE detalle_devolucion_compras
    ADD CONSTRAINT chk_dev_compras_no_negativo CHECK (cantidad_devuelta >= 0);

-- Validación de stock crítico
ALTER TABLE inventario_sucursal
    ADD CONSTRAINT chk_stock_critico CHECK (stock_critico >= 0);


-- ===========================================
-- AJUSTES EN FECHAS
-- ===========================================

ALTER TABLE producto
    ALTER COLUMN fecha_actualizacion SET DEFAULT NOW();


-- ===========================================
-- REGLAS DE NEGOCIO (VERIFICACIONES)
-- ===========================================

-- Metodo de pago
ALTER TABLE venta
    ADD CONSTRAINT chk_metodo_pago
    CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'MIXTO'));

-- Tipo de movimiento
ALTER TABLE historial_movimientos
    ADD CONSTRAINT chk_tipo_mov
    CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA'));

-- Tipo de devolución
ALTER TABLE devolucion_ventas
    ADD CONSTRAINT chk_tipo_dev_venta
    CHECK (tipo_devolucion IN ('PARCIAL', 'TOTAL'));

ALTER TABLE devolucion_compras
    ADD CONSTRAINT chk_tipo_dev_compra
    CHECK (tipo_devolucion IN ('PARCIAL', 'TOTAL'));


-- ===========================================
-- AJUSTES FINALES
-- ===========================================

-- Sincronizar claves foráneas faltantes
ALTER TABLE detalle_compra
    DROP CONSTRAINT IF EXISTS detalle_compra_compra_id_fkey,
    DROP CONSTRAINT IF EXISTS detalle_compra_producto_id_fkey;

ALTER TABLE detalle_compra
    ADD CONSTRAINT detalle_compra_compra_id_fkey
        FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE;

ALTER TABLE detalle_compra
    ADD CONSTRAINT detalle_compra_producto_id_fkey
        FOREIGN KEY (producto_id) REFERENCES producto(id);

-- Detalle venta FKs
ALTER TABLE detalle_venta
    DROP CONSTRAINT IF EXISTS detalle_venta_venta_id_fkey,
    DROP CONSTRAINT IF EXISTS detalle_venta_producto_id_fkey;

ALTER TABLE detalle_venta
    ADD CONSTRAINT detalle_venta_venta_id_fkey
        FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE;

ALTER TABLE detalle_venta
    ADD CONSTRAINT detalle_venta_producto_id_fkey
        FOREIGN KEY (producto_id) REFERENCES producto(id);

-- ================================
-- VERIFICACIÓN DE ESTRUCTURA LISTA
-- ================================

COMMENT ON SCHEMA public IS 'Mi Tienda - Estructura inicial completa generada por Flyway V4';
