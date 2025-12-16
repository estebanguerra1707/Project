-- ==============================================================
-- V2__seed_data.sql
-- Seed inicial para Mi Tienda
-- ==============================================================

SET search_path TO public;

-- ==============================================================
-- 1. MÉTODOS DE PAGO
-- ==============================================================

INSERT INTO metodo_pago (name, activo)
VALUES
  ('EFECTIVO', TRUE),
  ('TARJETA', TRUE),
  ('TRANSFERENCIA', TRUE)
ON CONFLICT (name) DO NOTHING;

-- ==============================================================
-- 2. TIPOS DE NEGOCIO
-- ==============================================================

INSERT INTO business_type (name, active, code)
VALUES
    ('Papelería', TRUE, 'PAP'),
    ('Abarrotes', TRUE, 'ABA'),
    ('Ferretería', TRUE, 'FER'),
    ('Farmacia', TRUE, 'FAR'),
    ('Refaccionaria', TRUE, 'REF')
ON CONFLICT (name) DO NOTHING;

-- ==============================================================
-- 3. CATEGORÍAS BASE POR TIPO DE NEGOCIO
-- ==============================================================

-- Papelería
INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Útiles Escolares', TRUE, id FROM business_type WHERE code = 'PAP'
ON CONFLICT (name, business_type_id) DO NOTHING;

INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Arte y Manualidades', TRUE, id FROM business_type WHERE code = 'PAP'
ON CONFLICT (name, business_type_id) DO NOTHING;

-- Abarrotes
INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Abarrotes Generales', TRUE, id FROM business_type WHERE code = 'ABA'
ON CONFLICT (name, business_type_id) DO NOTHING;

-- Ferretería
INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Herramientas', TRUE, id FROM business_type WHERE code = 'FER'
ON CONFLICT (name, business_type_id) DO NOTHING;

-- Farmacia
INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Medicamentos', TRUE, id FROM business_type WHERE code = 'FAR'
ON CONFLICT (name, business_type_id) DO NOTHING;

INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Material de Curación', TRUE, id FROM business_type WHERE code = 'FAR'
ON CONFLICT (name, business_type_id) DO NOTHING;

-- Refaccionaria
INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Filtros', TRUE, id FROM business_type WHERE code = 'REF'
ON CONFLICT (name, business_type_id) DO NOTHING;

INSERT INTO product_category (name, activo, business_type_id)
SELECT 'Aceites', TRUE, id FROM business_type WHERE code = 'REF'
ON CONFLICT (name, business_type_id) DO NOTHING;

-- ==============================================================
-- 4. SUCURSAL PRINCIPAL
-- ==============================================================

INSERT INTO sucursal (name, address, phone, active, alerta_stock_critico, business_type_id)
SELECT 'Sucursal Principal', 'Sin dirección', '0000000000', TRUE, FALSE, id
FROM business_type WHERE code = 'REF'
ON CONFLICT (name) DO NOTHING;

-- ==============================================================
-- 5. PROVEEDOR GENÉRICO
-- ==============================================================

INSERT INTO proveedor (name, contact, email, active)
VALUES
    ('Generico', 'Contacto 1', 'proveedor1@mail.com', TRUE)
ON CONFLICT (name) DO NOTHING;