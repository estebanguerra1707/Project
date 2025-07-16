INSERT INTO business_type (id, name, active) VALUES (1, 'Papelería', true);

INSERT INTO sucursal (id, name, business_type_id, active, alerta_stock_critico)
VALUES (1, 'Sucursal Central', 1, true, true);

INSERT INTO usuario (id, username, email, password, branch_id, active)
VALUES (1, 'admin', 'admin@example.com', 'encodedPassword', 1, true);

INSERT INTO metodo_pago (id, name, active) VALUES (1, 'EFECTIVO', true);

-- Otros datos como productos, clientes, etc. pueden añadirse según se necesite