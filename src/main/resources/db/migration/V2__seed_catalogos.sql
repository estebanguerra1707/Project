-- =========================
--  SEED INICIAL (IDEMPOTENTE)
-- =========================

-- business_type
INSERT INTO public.business_type VALUES (7, 'Refaccionaria', true, 'REFACCIONARIA') ON CONFLICT DO NOTHING;
INSERT INTO public.business_type VALUES (1, 'Papeleria',     true, 'PAPELERIA')     ON CONFLICT DO NOTHING;
INSERT INTO public.business_type VALUES (2, 'Abarrotes',     true, 'ABARROTES')     ON CONFLICT DO NOTHING;
INSERT INTO public.business_type VALUES (3, 'Ferreteria',    true, 'FERRETERIA')    ON CONFLICT DO NOTHING;
INSERT INTO public.business_type VALUES (8, 'Farmacia',      true, 'FARMACIA')      ON CONFLICT DO NOTHING;

-- cliente
INSERT INTO public.cliente VALUES (1, false, NULL, NULL, NULL, NULL) ON CONFLICT DO NOTHING;
INSERT INTO public.cliente VALUES (3, true, 'Es un cliente', 'jose.suarez@gmail.com', 'Jose Suarez', '5512345678') ON CONFLICT DO NOTHING;
INSERT INTO public.cliente VALUES (2, true, 'cliente en espera', 'prueba_cliente@cliente.com', 'Juan Rosas', '5560606060') ON CONFLICT DO NOTHING;

-- compra (recorte)
INSERT INTO public.compra VALUES (8, 2, '2025-07-02 15:30:55', 3200.00, true, 7, 1, 3300.00, 100.00, 1) ON CONFLICT DO NOTHING;
INSERT INTO public.compra VALUES (1, 2, '2025-06-24 00:00:00',   31.50, true, 7, 1,   31.50,   0.00, 1) ON CONFLICT DO NOTHING;
-- ...

-- detalle_compra (recorte)
INSERT INTO public.detalle_compra VALUES (9, 1, 100, 32.00, 3200.00, true, 8) ON CONFLICT DO NOTHING;
INSERT INTO public.detalle_compra VALUES (1, 3,   2, 15.75,   31.50, true, 1) ON CONFLICT DO NOTHING;
-- ...

-- detalle_devolucion_compras (tiene IDENTITY → mantener OVERRIDING)
INSERT INTO public.detalle_devolucion_compras OVERRIDING SYSTEM VALUE
VALUES (5, 28, 2, 20.99, 65, 5) ON CONFLICT DO NOTHING;
INSERT INTO public.detalle_devolucion_compras OVERRIDING SYSTEM VALUE
VALUES (1, 28, 1, 20.99, 63, 1) ON CONFLICT DO NOTHING;
-- ...

-- product_category (recorte)
INSERT INTO public.product_category VALUES (1,  'Cuadernos', 1) ON CONFLICT DO NOTHING;
INSERT INTO public.product_category VALUES (2,  'Plumas',    1) ON CONFLICT DO NOTHING;
INSERT INTO public.product_category VALUES (27, 'Analgesicos', 8) ON CONFLICT DO NOTHING;
-- ...

-- producto (recorte)
INSERT INTO public.producto VALUES (1, 'Coca-Cola 2.5L', 'AB001', 'Refresco Coca-Cola presentación familiar', 32.00, '2025-06-21 00:00:00', true, 2, 3, 3, 2, '7501234567890', 40.00) ON CONFLICT DO NOTHING;
INSERT INTO public.producto VALUES (28, 'Paracetamol 500mg', 'FAR-001', 'Paracetamol generico de 500mg', 20.99, '2025-07-18 11:12:10.462556', true, 8, 33, 16, 5, '7501234567903', 25.99) ON CONFLICT DO NOTHING;
-- ...

-- devolucion_compras (mantener OVERRIDING si existe identity)
INSERT INTO public.devolucion_compras OVERRIDING SYSTEM VALUE
VALUES (1, '2025-08-15 13:50:41.022', 'Producto defectuoso', 'PARCIAL', 41.98, 36, 9, 5) ON CONFLICT DO NOTHING;
-- ...

-- venta (recorte)
INSERT INTO public.venta VALUES (55, '2025-07-14 14:41:55', 1799.99, true, 7, 2, 1, 0.00, 0.00, 2) ON CONFLICT DO NOTHING;
-- ...

-- =========================
--  AJUSTE DE SECUENCIAS (dejar al final)
-- =========================
SELECT pg_catalog.setval('public.business_type_id_seq', 8, true);
SELECT pg_catalog.setval('public.cliente_id_seq', 3, true);
SELECT pg_catalog.setval('public.compra_id_seq', 38, true);
SELECT pg_catalog.setval('public.detalle_compra_id_seq', 65, true);
SELECT pg_catalog.setval('public.detalle_devolucion_compras_id_seq', 5, true);
SELECT pg_catalog.setval('public.detalle_devolucion_ventas_id_seq', 2, true);
SELECT pg_catalog.setval('public.detalle_venta_id_seq', 124, true);
SELECT pg_catalog.setval('public.devolucion_compras_id_seq', 5, true);
SELECT pg_catalog.setval('public.devolucion_ventas_id_seq', 2, true);
SELECT pg_catalog.setval('public.forma_farmaceutica_id_seq', 1, true);
SELECT pg_catalog.setval('public.historial_movimiento_id_seq', 86, true);
SELECT pg_catalog.setval('public.inventario_farmacia_id_seq', 1, false);
SELECT pg_catalog.setval('public.inventario_sucursal_id_seq', 19, true);
SELECT pg_catalog.setval('public.payment_method_id_seq', 5, true);
SELECT pg_catalog.setval('public.principio_activo_id_seq', 1, true);
SELECT pg_catalog.setval('public.product_category_id_seq', 33, true);
SELECT pg_catalog.setval('public.product_detail_id_seq', 2, true);
SELECT pg_catalog.setval('public.producto_farmacia_id_seq', 1, false);
SELECT pg_catalog.setval('public.producto_id_seq', 28, true);
SELECT pg_catalog.setval('public.proveedor_id_seq', 16, true);
SELECT pg_catalog.setval('public.proveedor_sucursal_id_seq', 15, true);
SELECT pg_catalog.setval('public.sucursal_id_seq', 5, true);
SELECT pg_catalog.setval('public.unidad_medida_id_seq', 1, true);
SELECT pg_catalog.setval('public.usuario_id_seq', 9, true);
SELECT pg_catalog.setval('public.venta_id_seq', 76, true);
