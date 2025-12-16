-- ======================================================
-- V1__init_full_structure.sql
-- Estructura base Mi Tienda - Versión Final
-- ======================================================

SET search_path TO public;

-- ======================================================
-- 1. TABLAS DE CATÁLOGO BÁSICAS
-- ======================================================

CREATE TABLE business_type (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    code        VARCHAR(100) NOT NULL,
    CONSTRAINT uq_business_type_name UNIQUE(name),
    CONSTRAINT uq_business_type_code UNIQUE(code)
);

CREATE TABLE metodo_pago (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_metodo_pago_name UNIQUE(name)
);

CREATE TABLE forma_farmaceutica (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL
);

CREATE TABLE principio_activo (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL
);

CREATE TABLE unidad_medida (
    id          BIGSERIAL PRIMARY KEY,
    unidad      VARCHAR(100) NOT NULL
);

-- ======================================================
-- 2. CLIENTES, USUARIOS, SUCURSALES, PROVEEDORES
-- ======================================================

CREATE TABLE sucursal (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    address                 VARCHAR(500),
    phone                   VARCHAR(50),
    active                  BOOLEAN,
    alerta_stock_critico    BOOLEAN,
    business_type_id        BIGINT,
    CONSTRAINT fk_sucursal_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_type(id),
    CONSTRAINT uq_sucursal_name UNIQUE(name)
);

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(255) UNIQUE,
    email           VARCHAR(255) UNIQUE,
    password        VARCHAR(255),
    role            VARCHAR(50),
    active          BOOLEAN,
    branch_id       BIGINT,
    CONSTRAINT fk_usuario_branch
        FOREIGN KEY (branch_id) REFERENCES sucursal(id)
);

CREATE TABLE cliente (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    contact     VARCHAR(255),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    email       VARCHAR(255),
    phone       VARCHAR(50)
);

CREATE TABLE proveedor (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    contact     VARCHAR(255),
    active      BOOLEAN,
    email       VARCHAR(255),
    CONSTRAINT uq_proveedor_name UNIQUE(name)
);

CREATE TABLE proveedor_sucursal (
    id              BIGSERIAL PRIMARY KEY,
    proveedor_id    BIGINT NOT NULL,
    sucursal_id     BIGINT NOT NULL,
    CONSTRAINT fk_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
    CONSTRAINT fk_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id),
    CONSTRAINT uq_proveedor_sucursal UNIQUE (proveedor_id, sucursal_id)
);

-- ======================================================
-- 3. PRODUCTOS Y CATEGORÍAS
-- ======================================================

CREATE TABLE product_category (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    business_type_id    BIGINT,
    CONSTRAINT fk_category_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_type(id),
    CONSTRAINT uq_category_name_business UNIQUE(name, business_type_id)
);

CREATE TABLE producto (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    sku                 VARCHAR(255),
    description         TEXT,
    purchase_price      NUMERIC(18, 2),
    sale_price          NUMERIC(18, 2),
    active              BOOLEAN,
    creation_date       TIMESTAMP,
    codigo_barras       VARCHAR(255) UNIQUE,
    category_id         BIGINT,
    provider_id         BIGINT,
    branch_id           BIGINT,
    business_type_id    BIGINT,
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES product_category(id),
    CONSTRAINT fk_product_provider
        FOREIGN KEY (provider_id) REFERENCES proveedor(id),
    CONSTRAINT fk_product_branch
        FOREIGN KEY (branch_id) REFERENCES sucursal(id),
    CONSTRAINT fk_product_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_type(id)
);

CREATE INDEX idx_producto_codigo_barras ON producto(codigo_barras);
CREATE INDEX idx_producto_category_id ON producto(category_id);
CREATE INDEX idx_producto_provider_id ON producto(provider_id);

CREATE TABLE product_detail (
    id                      BIGSERIAL PRIMARY KEY,
    product_id              BIGINT UNIQUE,
    part_number             VARCHAR(255),
    car_brand               VARCHAR(255),
    car_model               VARCHAR(255),
    year_range              VARCHAR(50),
    oem_equivalent          VARCHAR(255),
    technical_description   TEXT,
    CONSTRAINT fk_product_detail_product
        FOREIGN KEY (product_id) REFERENCES producto(id)
);

CREATE TABLE producto_farmacia (
    id                      BIGINT PRIMARY KEY,
    concentracion           VARCHAR(255),
    principio_activo_id     BIGINT,
    forma_farmaceutica_id   BIGINT,
    unidad_medida_id        BIGINT,
    CONSTRAINT fk_pf_producto
        FOREIGN KEY (id) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT fk_pf_principio
        FOREIGN KEY (principio_activo_id) REFERENCES principio_activo(id),
    CONSTRAINT fk_pf_forma
        FOREIGN KEY (forma_farmaceutica_id) REFERENCES forma_farmaceutica(id),
    CONSTRAINT fk_pf_unidad
        FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida(id)
);

-- ======================================================
-- 4. INVENTARIOS E HISTORIAL
-- ======================================================

CREATE TABLE inventario_sucursal (
    id                  BIGSERIAL PRIMARY KEY,
    stock               INTEGER,
    min_stock           INTEGER,
    max_stock           INTEGER,
    stock_critico       BOOLEAN NOT NULL DEFAULT FALSE,
    last_updated_by     VARCHAR(255),
    last_updated_date   TIMESTAMP,
    product_id          BIGINT,
    branch_id           BIGINT,
    CONSTRAINT fk_inventario_producto
        FOREIGN KEY (product_id) REFERENCES producto(id),
    CONSTRAINT fk_inventario_sucursal
        FOREIGN KEY (branch_id) REFERENCES sucursal(id)
);

CREATE UNIQUE INDEX uq_inventario_producto_sucursal
    ON inventario_sucursal(product_id, branch_id);

CREATE INDEX idx_inventario_product_id ON inventario_sucursal(product_id);
CREATE INDEX idx_inventario_branch_id ON inventario_sucursal(branch_id);

CREATE TABLE inventario_farmacia (
    id                  BIGSERIAL PRIMARY KEY,
    producto_id         BIGINT,
    sucursal_id         BIGINT,
    lote                VARCHAR(255),
    fecha_caducidad     DATE,
    cantidad            INTEGER,
    CONSTRAINT fk_inv_farmacia_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_inv_farmacia_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)
);

CREATE INDEX idx_inv_farmacia_producto ON inventario_farmacia(producto_id);
CREATE INDEX idx_inv_farmacia_sucursal ON inventario_farmacia(sucursal_id);

CREATE TABLE historial_movimiento (
    id                      BIGSERIAL PRIMARY KEY,
    movement_date           TIMESTAMP,
    movement_type           VARCHAR(50),
    quantity                INTEGER,
    before_stock            INTEGER,
    new_stock               INTEGER,
    reference               VARCHAR(255),
    inventario_sucursal_id  BIGINT,
    CONSTRAINT fk_historial_inventario
        FOREIGN KEY (inventario_sucursal_id) REFERENCES inventario_sucursal(id)
);

CREATE INDEX idx_historial_inventario ON historial_movimiento(inventario_sucursal_id);
CREATE INDEX idx_historial_movement_date ON historial_movimiento(movement_date);

-- ======================================================
-- 5. COMPRAS / DETALLES
-- ======================================================

CREATE TABLE compra (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_date       TIMESTAMP,
    total_amount        NUMERIC(18, 2),
    active              BOOLEAN,
    amount_paid         NUMERIC(18, 2),
    change_amount       NUMERIC(18, 2),
    payment_method_id   BIGINT,
    supplier_id         BIGINT,
    usuario_id          BIGINT,
    branch_id           BIGINT,
    CONSTRAINT fk_compra_metodo_pago
        FOREIGN KEY (payment_method_id) REFERENCES metodo_pago(id),
    CONSTRAINT fk_compra_proveedor
        FOREIGN KEY (supplier_id) REFERENCES proveedor(id),
    CONSTRAINT fk_compra_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_compra_sucursal
        FOREIGN KEY (branch_id) REFERENCES sucursal(id)
);

CREATE INDEX idx_compra_branch_id ON compra(branch_id);
CREATE INDEX idx_compra_supplier_id ON compra(supplier_id);

CREATE TABLE detalle_compra (
    id              BIGSERIAL PRIMARY KEY,
    purchase_id     BIGINT,
    product_id      BIGINT,
    quantity        INTEGER,
    unit_price      NUMERIC(18, 2),
    subtotal        NUMERIC(18, 2),
    active          BOOLEAN,
    CONSTRAINT fk_detalle_compra
        FOREIGN KEY (purchase_id) REFERENCES compra(id),
    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (product_id) REFERENCES producto(id)
);

CREATE INDEX idx_detalle_compra_purchase ON detalle_compra(purchase_id);
CREATE INDEX idx_detalle_compra_product ON detalle_compra(product_id);

-- ======================================================
-- 6. VENTAS / DETALLES
-- ======================================================

CREATE TABLE venta (
    id                  BIGSERIAL PRIMARY KEY,
    sale_date           TIMESTAMP,
    total_amount        NUMERIC(18, 2),
    active              BOOLEAN,
    amount_paid         NUMERIC(18, 2),
    change_amount       NUMERIC(18, 2),
    payment_method_id   BIGINT,
    cliente_id          BIGINT,
    usuario_id          BIGINT,
    branch_id           BIGINT,
    CONSTRAINT fk_venta_metodo_pago
        FOREIGN KEY (payment_method_id) REFERENCES metodo_pago(id),
    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_venta_sucursal
        FOREIGN KEY (branch_id) REFERENCES sucursal(id)
);

CREATE INDEX idx_venta_branch_id ON venta(branch_id);
CREATE INDEX idx_venta_cliente_id ON venta(cliente_id);

CREATE TABLE detalle_venta (
    id                  BIGSERIAL PRIMARY KEY,
    quantity            INTEGER,
    unit_price          NUMERIC(18, 2),
    sub_total           NUMERIC(18, 2),
    cantidad_devuelta   INTEGER,
    active              BOOLEAN,
    sale_id             BIGINT,
    product_id          BIGINT,
    CONSTRAINT fk_detalle_venta
        FOREIGN KEY (sale_id) REFERENCES venta(id),
    CONSTRAINT fk_detalle_producto_venta
        FOREIGN KEY (product_id) REFERENCES producto(id)
);

CREATE INDEX idx_detalle_venta_sale ON detalle_venta(sale_id);
CREATE INDEX idx_detalle_venta_product ON detalle_venta(product_id);

-- ======================================================
-- 7. DEVOLUCIONES
-- ======================================================

CREATE TABLE devolucion_compras (
    id                  BIGSERIAL PRIMARY KEY,
    fecha               TIMESTAMP,
    motivo              TEXT,
    tipo_devolucion     VARCHAR(50),
    monto_devuelto      NUMERIC(18, 2),
    compra_id           BIGINT NOT NULL,
    usuario_id          BIGINT NOT NULL,
    branch_id           BIGINT NOT NULL,
    CONSTRAINT fk_devolucion_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id),
    CONSTRAINT fk_devolucion_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_devolucion_branch
        FOREIGN KEY (branch_id) REFERENCES sucursal(id)
);

CREATE TABLE detalle_devolucion_compras (
    id                  BIGSERIAL PRIMARY KEY,
    producto_id         BIGINT,
    cantidad_devuelta   INTEGER,
    precio_compra       NUMERIC(18, 2),
    detalle_compra_id   BIGINT,
    devolucion_id       BIGINT NOT NULL,
    CONSTRAINT fk_detalle_devolucion_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_detalle_devolucion_compra_detalle
        FOREIGN KEY (detalle_compra_id) REFERENCES detalle_compra(id),
    CONSTRAINT fk_detalle_devolucion_compra
        FOREIGN KEY (devolucion_id) REFERENCES devolucion_compras(id)
);

CREATE TABLE devolucion_ventas (
    id                  BIGSERIAL PRIMARY KEY,
    fecha_devolucion    TIMESTAMP,
    motivo              TEXT,
    monto_devuelto      NUMERIC(18, 2),
    venta_id            BIGINT NOT NULL,
    usuario_id          BIGINT NOT NULL,
    branch_id           BIGINT NOT NULL,
    tipo_devolucion     VARCHAR(50),
    CONSTRAINT fk_devolucion_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_devolucion_usuario_venta
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_devolucion_branch_venta
        FOREIGN KEY (branch_id) REFERENCES sucursal(id)
);

CREATE TABLE detalle_devolucion_ventas (
    id                  BIGSERIAL PRIMARY KEY,
    producto_id         BIGINT,
    cantidad_devuelta   INTEGER,
    precio_unitario     NUMERIC(18, 2),
    detalle_venta_id    BIGINT NOT NULL,
    devolucion_id       BIGINT NOT NULL,
    CONSTRAINT fk_detalle_devolucion_venta_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_devolucion_detalle_venta
        FOREIGN KEY (detalle_venta_id) REFERENCES detalle_venta(id),
    CONSTRAINT fk_detalle_devolucion_venta
        FOREIGN KEY (devolucion_id) REFERENCES devolucion_ventas(id)
);

-- ======================================================
-- 8. PASSWORD RESET
-- ======================================================

CREATE TABLE password_reset_token (
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(255) NOT NULL,
    expiry_date     TIMESTAMP NOT NULL,
    used            BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_id      BIGINT UNIQUE,
    CONSTRAINT fk_password_token_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE password_reset_audit (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    ip_address      VARCHAR(255),
    requested_at    TIMESTAMP NOT NULL
);

-- ======================================================
-- FIN V1
-- ======================================================
