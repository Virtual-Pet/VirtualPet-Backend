-- =============================================================================
-- V1.6 — Datos dummy adicionales para entorno de prueba/demo
--
-- Convenciones de UUIDs hardcodeados (para trazabilidad):
--   a0eebc99-...-a15..a19  → nuevas categorías
--   b0eebc99-...-b24..b2d  → nuevos productos
--   c0eebc99-...-c34..c42  → nuevas variantes
--   d0eebc99-...-d050..    → nuevas imágenes
--   e0eebc99-...-e01..e13  → usuarios staff/admin
--   e0eebc99-...-c01..c0f  → usuarios customers
--   f0eebc99-...-a01..     → addresses
--   1.... → orders   2.... → order_items   3.... → payments
--   4.... → shipments  5.... → assignments  6.... → attempts
--   7.... → order_status_history  8.... → payment_status_history
--   9.... → shipment_status
--
-- Password de TODOS los usuarios sembrados aquí: Password123!
--   Hash bcrypt (10 rounds): $2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm
-- =============================================================================


-- =============================================================================
-- 1. CATÁLOGO — Categorías nuevas
-- =============================================================================
INSERT INTO catalog.categories (id, name, slug, father_id, active) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Higiene',        'higiene',        NULL,                                   TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'Higiene Perros', 'higiene-perros', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'Higiene Gatos',  'higiene-gatos',  'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'Juguetes',       'juguetes',       NULL,                                   TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'Snacks',         'snacks',         NULL,                                   TRUE);


-- =============================================================================
-- 2. CATÁLOGO — Productos nuevos
-- =============================================================================
INSERT INTO catalog.products (id, name, description, brand, pet_type, category_id, active) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24', 'Champú Hipoalergénico Pelaje Largo', 'Champú suave para perros de pelaje largo, con avena coloidal y aloe vera. pH balanceado.',          'PetClean',  'Perro',      'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25', 'Arena Sanitaria Aglomerante',        'Arena aglomerante de bentonita con control de olor. Bolsa rendidora para uso diario.',              'CleanCat',  'Gato',       'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26', 'Pelota Goma Mordedor',               'Pelota de goma maciza con relieve para masaje de encías. Resistente a la mordida.',                 'PlayDog',   'Perro',      'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b27', 'Ratón de Tela con Catnip',           'Juguete de tela rellena con catnip natural. Estimula el instinto de caza del gato.',                'CatPlay',   'Gato',       'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28', 'Galletitas Perro Hueso',             'Galletitas crocantes con forma de hueso. Premio ideal para entrenamiento.',                         'SnackPet',  'Perro',      'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b29', 'Snacks Gatos Sabor Atún',            'Premios crocantes con relleno cremoso de atún. Sin colorantes artificiales.',                       'SnackPet',  'Gato',       'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2a', 'Toallitas Húmedas para Mascotas',    'Toallitas sin alcohol para limpieza diaria de patas, pelaje y orejas.',                             'PetClean',  'Perro/Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2b', 'Cepillo Quitapelo Doble Cara',       'Cepillo ergonómico con cerdas finas y púas para remover pelo muerto y desenredar.',                 'PetGroom',  'Perro/Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c', 'Hueso de Cuero Bovino',              'Hueso prensado de cuero natural. Ayuda a la higiene dental y al ejercicio mandibular.',             'NaturePet', 'Perro',      'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2d', 'Pelota Plástica con Sonido',         'Pelota liviana con cascabel interno. Ideal para jugar al traer.',                                   'PlayDog',   'Perro',      'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', TRUE);


-- =============================================================================
-- 3. CATÁLOGO — Variantes nuevas
-- =============================================================================
INSERT INTO catalog.product_variants (id, product_id, sku, attributes, price, stock, stock_min, image_path, active) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24', 'CHAMPU-HIPO-500ML',  '{"volumen":"500ml"}',                        5800.00,  40,  5,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c35', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24', 'CHAMPU-HIPO-1L',     '{"volumen":"1L"}',                          10200.00,  20,  3,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c35/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25', 'ARENA-AGLO-5KG',     '{"peso":"5kg"}',                             4900.00,  80, 10,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c37', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25', 'ARENA-AGLO-10KG',    '{"peso":"10kg"}',                            9100.00,  35,  5,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c37/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c38', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26', 'PELOTA-GOMA-M',      '{"talle":"M","color":"Rojo"}',               2500.00,  60, 10,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c38/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c39', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26', 'PELOTA-GOMA-L',      '{"talle":"L","color":"Azul"}',               3200.00,  45,  8,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c39/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3a', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b27', 'RATON-CATNIP-PACK3', '{"unidades":"3","colores":"Surtidos"}',      1800.00, 120, 15,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b27/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3a/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28', 'GALL-PER-500G',      '{"peso":"500g","sabor":"Carne"}',            2200.00,  70, 12,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3c', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28', 'GALL-PER-1KG',       '{"peso":"1kg","sabor":"Carne"}',             3900.00,  40,  8,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3c/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b29', 'SNACK-GAT-ATUN-60G', '{"peso":"60g","sabor":"Atún"}',              1500.00, 100, 20,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b29/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3e', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2a', 'TOALL-HUM-100U',     '{"unidades":"100"}',                         3400.00,  90, 15,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2a/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3e/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2b', 'CEPILLO-DBL-M',      '{"talle":"M","material":"Plástico/Acero"}',  4600.00,  30,  5,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2b/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c40', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c', 'HUESO-CUERO-M',      '{"talle":"M","largo":"15cm"}',               2900.00,  50,  8,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c40/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c41', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c', 'HUESO-CUERO-L',      '{"talle":"L","largo":"25cm"}',               4500.00,  25,  4,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c41/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2d', 'PELOTA-PLAS-SON',    '{"talle":"M","color":"Verde"}',              1900.00,  80, 12,  '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2d/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42/1.jpg', TRUE);


-- =============================================================================
-- 4. CATÁLOGO — Imágenes nuevas
-- =============================================================================
INSERT INTO catalog.image_variants (id, product_variant_id, image_path, is_main, order_img) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380050', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380051', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380052', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c35', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b24/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c35/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380053', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380054', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c37', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b25/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c37/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380055', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c38', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c38/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380056', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c39', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b26/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c39/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380057', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3a', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b27/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3a/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380058', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380059', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3c', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b28/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3c/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd38005a', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b29/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd38005b', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3e', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2a/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3e/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd38005c', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2b/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd38005d', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c40', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c40/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd38005e', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c41', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2c/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c41/1.jpg', TRUE,  1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd38005f', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b2d/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42/1.jpg', TRUE,  1);


-- =============================================================================
-- 5. SHIPMENTS — Warehouses adicionales (id=2, id=3)
-- =============================================================================
INSERT INTO shipments.warehouses (name, city, state, zip_code) VALUES
('Sucursal CABA Norte',      'Ciudad Autónoma de Buenos Aires', 'CABA',    '1428'),
('Sucursal Córdoba Capital', 'Córdoba',                         'Córdoba', '5000');


-- =============================================================================
-- 6. AUTH — Usuarios (1 admin, 3 empleados, 15 clientes)
-- =============================================================================
INSERT INTO auth.users (id, email, password_hash, role, active) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e01', 'admin2@virtualpet.com',     '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_ADMIN',    TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'operario1@virtualpet.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_EMPLOYEE', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', 'operario2@virtualpet.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_EMPLOYEE', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', 'operario3@virtualpet.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_EMPLOYEE', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', 'lucia.gomez@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c02', 'martin.perez@example.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'sofia.lopez@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'juan.romero@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'camila.ruiz@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'nicolas.diaz@example.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'agustina.f@example.com',    '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'matias.silva@example.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'florencia.b@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0a', 'rodrigo.alv@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', 'valentina.m@example.com',   '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0c', 'tomas.castro@example.com',  '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', 'bianca.aguero@example.com', '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0e', 'ezequiel.b@example.com',    '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0f', 'paula.molinas@example.com', '$2b$10$wPCOlTXJtYIVIVfYrHFLeOVwflcne6UjNn76TVmhGKk3lpI/u9dLm', 'ROLE_CUSTOMER', TRUE);


-- =============================================================================
-- 7. AUTH — Customers (perfil de los 15 clientes)
-- =============================================================================
INSERT INTO auth.customers (user_id, name, lastname, dni, phone) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', 'Lucía',     'Gómez',     '30123456', '2235551001'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c02', 'Martín',    'Pérez',     '28456789', '2235551002'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'Sofía',     'López',     '32987654', '1135551003'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'Juan',      'Romero',    '27345678', '3515551004'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'Camila',    'Ruiz',      '33765432', '2235551005'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'Nicolás',   'Díaz',      '29876543', '1135551006'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'Agustina',  'Fernández', '31234567', '2235551007'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'Matías',    'Silva',     '26543210', '3515551008'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'Florencia', 'Báez',      '34567890', '1135551009'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0a', 'Rodrigo',   'Álvarez',   '25432109', '2235551010'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', 'Valentina', 'Méndez',    '35678901', '1135551011'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0c', 'Tomás',     'Castro',    '30987612', '3515551012'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', 'Bianca',    'Agüero',    '36789012', '2235551013'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0e', 'Ezequiel',  'Benítez',   '24321098', '1135551014'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0f', 'Paula',     'Molinas',   '32198765', '2235551015');


-- =============================================================================
-- 8. AUTH — Employees (linkeados a warehouses)
-- =============================================================================
INSERT INTO auth.employees (user_id, name, lastname, legajo, warehouse_id) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e01', 'Admin',  'Secundario', 'ADM-0001', 1),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'Carlos', 'Rodríguez',  'OPR-0001', 1),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', 'Lucía',  'Fernández',  'OPR-0002', 1),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', 'Diego',  'Sosa',       'OPR-0003', 2);


-- =============================================================================
-- 9. AUTH — Addresses (1-2 por customer, una marcada is_default)
-- =============================================================================
INSERT INTO auth.addresses (id, user_id, street, num, city, zip_code, is_default) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', 'Av. Independencia',   '2345', 'Mar del Plata',                   '7600', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c02', 'Calle Belgrano',      '1820', 'Mar del Plata',                   '7600', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'Av. Corrientes',      '4567', 'Ciudad Autónoma de Buenos Aires', '1414', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'Calle Lavalle',       '789',  'Ciudad Autónoma de Buenos Aires', '1047', FALSE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'Bv. San Juan',        '3201', 'Córdoba',                         '5000', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'Calle Salta',         '654',  'Mar del Plata',                   '7600', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'Av. Cabildo',         '2100', 'Ciudad Autónoma de Buenos Aires', '1428', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'Av. Colón',           '3500', 'Mar del Plata',                   '7600', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'Calle Mitre',         '1234', 'Mar del Plata',                   '7600', FALSE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a0a', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'Av. Vélez Sársfield', '500',  'Córdoba',                         '5000', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a0b', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'Calle Florida',       '850',  'Ciudad Autónoma de Buenos Aires', '1005', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a0c', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0a', 'Av. Luro',            '4321', 'Mar del Plata',                   '7600', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a0d', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', 'Av. Santa Fe',        '3000', 'Ciudad Autónoma de Buenos Aires', '1425', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a0e', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0c', 'Bv. Chacabuco',       '275',  'Córdoba',                         '5000', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a0f', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', 'Calle Alvear',        '912',  'Mar del Plata',                   '7600', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0e', 'Av. Rivadavia',       '6789', 'Ciudad Autónoma de Buenos Aires', '1406', TRUE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0e', 'Calle Esmeralda',     '450',  'Ciudad Autónoma de Buenos Aires', '1035', FALSE),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0f', 'Av. Constitución',    '5500', 'Mar del Plata',                   '7600', TRUE);


-- =============================================================================
-- 10. ORDERS — 20 órdenes (15 CONFIRMED + 5 CANCELLED)
--      total = SUM(order_items.subtotal). Mantener consistencia con la sección 11.
-- =============================================================================
INSERT INTO orders.orders (id, user_id, session_id, warehouse_id, contact_name, contact_lastname, contact_email, contact_phone, status, total, shipping_address, shipping_attempts, created_at, updated_at) VALUES
('10000000-0000-4000-8000-000000000001', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', '11111111-0000-4000-8000-000000000001', 1, 'Lucía',     'Gómez',     'lucia.gomez@example.com',   '2235551001', 'CONFIRMED', 60000.00,  'Av. Independencia 2345, Mar del Plata, BA, 7600',                   0, NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days'),
('10000000-0000-4000-8000-000000000002', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c02', '11111111-0000-4000-8000-000000000002', 1, 'Martín',    'Pérez',     'martin.perez@example.com',  '2235551002', 'CONFIRMED', 47000.00,  'Calle Belgrano 1820, Mar del Plata, BA, 7600',                      0, NOW() - INTERVAL '25 days', NOW() - INTERVAL '23 days'),
('10000000-0000-4000-8000-000000000003', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', '11111111-0000-4000-8000-000000000003', 2, 'Sofía',     'López',     'sofia.lopez@example.com',   '1135551003', 'CONFIRMED', 36700.00,  'Av. Corrientes 4567, CABA, 1414',                                   0, NOW() - INTERVAL '22 days', NOW() - INTERVAL '20 days'),
('10000000-0000-4000-8000-000000000004', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', '11111111-0000-4000-8000-000000000004', 3, 'Juan',      'Romero',    'juan.romero@example.com',   '3515551004', 'CONFIRMED', 110000.00, 'Bv. San Juan 3201, Córdoba, CBA, 5000',                             0, NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days'),
('10000000-0000-4000-8000-000000000005', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', '11111111-0000-4000-8000-000000000005', 1, 'Camila',    'Ruiz',      'camila.ruiz@example.com',   '2235551005', 'CONFIRMED', 24500.00,  'Calle Salta 654, Mar del Plata, BA, 7600',                          0, NOW() - INTERVAL '18 days', NOW() - INTERVAL '16 days'),
('10000000-0000-4000-8000-000000000006', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', '11111111-0000-4000-8000-000000000006', 2, 'Nicolás',   'Díaz',      'nicolas.diaz@example.com',  '1135551006', 'CONFIRMED', 18900.00,  'Av. Cabildo 2100, CABA, 1428',                                      0, NOW() - INTERVAL '15 days', NOW() - INTERVAL '13 days'),
('10000000-0000-4000-8000-000000000007', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c07', '11111111-0000-4000-8000-000000000007', 1, 'Agustina',  'Fernández', 'agustina.f@example.com',    '2235551007', 'CONFIRMED', 73600.00,  'Av. Colón 3500, Mar del Plata, BA, 7600',                           0, NOW() - INTERVAL '14 days', NOW() - INTERVAL '12 days'),
('10000000-0000-4000-8000-000000000008', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c08', '11111111-0000-4000-8000-000000000008', 3, 'Matías',    'Silva',     'matias.silva@example.com',  '3515551008', 'CONFIRMED', 9000.00,   'Av. Vélez Sársfield 500, Córdoba, CBA, 5000',                       0, NOW() - INTERVAL '12 days', NOW() - INTERVAL '10 days'),
('10000000-0000-4000-8000-000000000009', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c09', '11111111-0000-4000-8000-000000000009', 2, 'Florencia', 'Báez',      'florencia.b@example.com',   '1135551009', 'CONFIRMED', 14100.00,  'Calle Florida 850, CABA, 1005',                                     0, NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days'),
('10000000-0000-4000-8000-00000000000a', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0a', '11111111-0000-4000-8000-00000000000a', 1, 'Rodrigo',   'Álvarez',   'rodrigo.alv@example.com',   '2235551010', 'CONFIRMED', 35000.00,  'Av. Luro 4321, Mar del Plata, BA, 7600',                            0, NOW() - INTERVAL '9 days',  NOW() - INTERVAL '7 days'),
('10000000-0000-4000-8000-00000000000b', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', '11111111-0000-4000-8000-00000000000b', 2, 'Valentina', 'Méndez',    'valentina.m@example.com',   '1135551011', 'CONFIRMED', 52200.00,  'Av. Santa Fe 3000, CABA, 1425',                                     0, NOW() - INTERVAL '8 days',  NOW() - INTERVAL '6 days'),
('10000000-0000-4000-8000-00000000000c', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0c', '11111111-0000-4000-8000-00000000000c', 3, 'Tomás',     'Castro',    'tomas.castro@example.com',  '3515551012', 'CONFIRMED', 19800.00,  'Bv. Chacabuco 275, Córdoba, CBA, 5000',                             0, NOW() - INTERVAL '6 days',  NOW() - INTERVAL '4 days'),
('10000000-0000-4000-8000-00000000000d', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', '11111111-0000-4000-8000-00000000000d', 1, 'Bianca',    'Agüero',    'bianca.aguero@example.com', '2235551013', 'CONFIRMED', 42000.00,  'Calle Alvear 912, Mar del Plata, BA, 7600',                         0, NOW() - INTERVAL '5 days',  NOW() - INTERVAL '3 days'),
('10000000-0000-4000-8000-00000000000e', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0e', '11111111-0000-4000-8000-00000000000e', 2, 'Ezequiel',  'Benítez',   'ezequiel.b@example.com',    '1135551014', 'CONFIRMED', 7600.00,   'Av. Rivadavia 6789, CABA, 1406',                                    0, NOW() - INTERVAL '3 days',  NOW() - INTERVAL '2 days'),
('10000000-0000-4000-8000-00000000000f', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0f', '11111111-0000-4000-8000-00000000000f', 1, 'Paula',     'Molinas',   'paula.molinas@example.com', '2235551015', 'CONFIRMED', 28700.00,  'Av. Constitución 5500, Mar del Plata, BA, 7600',                    0, NOW() - INTERVAL '2 days',  NOW() - INTERVAL '1 day'),
('10000000-0000-4000-8000-000000000010', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', '11111111-0000-4000-8000-000000000010', 1, 'Lucía',     'Gómez',     'lucia.gomez@example.com',   '2235551001', 'CANCELLED', 15000.00,  'Av. Independencia 2345, Mar del Plata, BA, 7600',                   0, NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days'),
('10000000-0000-4000-8000-000000000011', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', '11111111-0000-4000-8000-000000000011', 2, 'Sofía',     'López',     'sofia.lopez@example.com',   '1135551003', 'CANCELLED', 4500.00,   'Av. Corrientes 4567, CABA, 1414',                                   0, NOW() - INTERVAL '21 days', NOW() - INTERVAL '21 days'),
('10000000-0000-4000-8000-000000000012', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', '11111111-0000-4000-8000-000000000012', 2, 'Nicolás',   'Díaz',      'nicolas.diaz@example.com',  '1135551006', 'CANCELLED', 33900.00,  'Av. Cabildo 2100, CABA, 1428',                                      0, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
('10000000-0000-4000-8000-000000000013', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', '11111111-0000-4000-8000-000000000013', 2, 'Valentina', 'Méndez',    'valentina.m@example.com',   '1135551011', 'CANCELLED', 8500.00,   'Av. Santa Fe 3000, CABA, 1425',                                     1, NOW() - INTERVAL '7 days',  NOW() - INTERVAL '7 days'),
('10000000-0000-4000-8000-000000000014', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', '11111111-0000-4000-8000-000000000014', 1, 'Bianca',    'Agüero',    'bianca.aguero@example.com', '2235551013', 'CANCELLED', 24500.00,  'Calle Alvear 912, Mar del Plata, BA, 7600',                         0, NOW() - INTERVAL '4 days',  NOW() - INTERVAL '4 days');


-- =============================================================================
-- 11. ORDERS — order_items (subtotal = unit_price * quantity exacto)
-- =============================================================================
INSERT INTO orders.order_items (id, order_id, product_variant_id, sku_snapshot, name_snapshot, unit_price, quantity, subtotal) VALUES
-- Order 1 (total 60000)
('20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'RC-AD-3KG',          'Royal Canin Adulto 3kg',              15000.00, 4, 60000.00),
-- Order 2 (total 47000 = 24000 + 18900 + 2200 + 1900)
('20000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', 'EXC-PER-ADU-3KG',    'Excellent Perro Adulto 3kg',          12000.00, 2, 24000.00),
('20000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', 'RC-KIT-GAT-1.5KG',   'Royal Canin Kitten 1.5kg',            18900.00, 1, 18900.00),
('20000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b', 'GALL-PER-500G',      'Galletitas Perro Hueso 500g',          2200.00, 1,  2200.00),
('20000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42', 'PELOTA-PLAS-SON',    'Pelota Plástica con Sonido',           1900.00, 1,  1900.00),
-- Order 3 (total 36700 = 24500 + 5800 + 4900 + 1500)
('20000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', 'PP-STER-GAT-3KG',    'Pro Plan Sterilized Gato 3kg',        24500.00, 1, 24500.00),
('20000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34', 'CHAMPU-HIPO-500ML',  'Champú Hipoalergénico 500ml',          5800.00, 1,  5800.00),
('20000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36', 'ARENA-AGLO-5KG',     'Arena Sanitaria Aglomerante 5kg',      4900.00, 1,  4900.00),
('20000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d', 'SNACK-GAT-ATUN-60G', 'Snacks Gatos Sabor Atún 60g',          1500.00, 1,  1500.00),
-- Order 4 (total 110000)
('20000000-0000-4000-8000-00000000000a', '10000000-0000-4000-8000-000000000004', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', 'CUCHA-MAD-L',        'Cucha de Madera Techo Dos Aguas L',  110000.00, 1,110000.00),
-- Order 5 (total 24500 = 10200 + 9100 + 2200 + 3000)
('20000000-0000-4000-8000-00000000000b', '10000000-0000-4000-8000-000000000005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c35', 'CHAMPU-HIPO-1L',     'Champú Hipoalergénico 1L',            10200.00, 1, 10200.00),
('20000000-0000-4000-8000-00000000000c', '10000000-0000-4000-8000-000000000005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c37', 'ARENA-AGLO-10KG',    'Arena Sanitaria Aglomerante 10kg',     9100.00, 1,  9100.00),
('20000000-0000-4000-8000-00000000000d', '10000000-0000-4000-8000-000000000005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b', 'GALL-PER-500G',      'Galletitas Perro Hueso 500g',          2200.00, 1,  2200.00),
('20000000-0000-4000-8000-00000000000e', '10000000-0000-4000-8000-000000000005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d', 'SNACK-GAT-ATUN-60G', 'Snacks Gatos Sabor Atún 60g',          1500.00, 2,  3000.00),
-- Order 6 (total 18900)
('20000000-0000-4000-8000-00000000000f', '10000000-0000-4000-8000-000000000006', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', 'RC-KIT-GAT-1.5KG',   'Royal Canin Kitten 1.5kg',            18900.00, 1, 18900.00),
-- Order 7 (total 73600 = 65000 + 2200 + 4500 + 1900)
('20000000-0000-4000-8000-000000000010', '10000000-0000-4000-8000-000000000007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c29', 'RASC-TORRE-120CM',   'Rascador Torre 3 Niveles con Cucha',  65000.00, 1, 65000.00),
('20000000-0000-4000-8000-000000000011', '10000000-0000-4000-8000-000000000007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b', 'GALL-PER-500G',      'Galletitas Perro Hueso 500g',          2200.00, 1,  2200.00),
('20000000-0000-4000-8000-000000000012', '10000000-0000-4000-8000-000000000007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c41', 'HUESO-CUERO-L',      'Hueso de Cuero Bovino L',              4500.00, 1,  4500.00),
('20000000-0000-4000-8000-000000000013', '10000000-0000-4000-8000-000000000007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42', 'PELOTA-PLAS-SON',    'Pelota Plástica con Sonido',           1900.00, 1,  1900.00),
-- Order 8 (total 9000)
('20000000-0000-4000-8000-000000000014', '10000000-0000-4000-8000-000000000008', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', 'COM-PLAS-MED',       'Comedero Plástico Mediano',            4500.00, 2,  9000.00),
-- Order 9 (total 14100 = 4600 + 5800 + 2200 + 1500)
('20000000-0000-4000-8000-000000000015', '10000000-0000-4000-8000-000000000009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f', 'CEPILLO-DBL-M',      'Cepillo Quitapelo Doble Cara M',       4600.00, 1,  4600.00),
('20000000-0000-4000-8000-000000000016', '10000000-0000-4000-8000-000000000009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c34', 'CHAMPU-HIPO-500ML',  'Champú Hipoalergénico 500ml',          5800.00, 1,  5800.00),
('20000000-0000-4000-8000-000000000017', '10000000-0000-4000-8000-000000000009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3b', 'GALL-PER-500G',      'Galletitas Perro Hueso 500g',          2200.00, 1,  2200.00),
('20000000-0000-4000-8000-000000000018', '10000000-0000-4000-8000-000000000009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3d', 'SNACK-GAT-ATUN-60G', 'Snacks Gatos Sabor Atún 60g',          1500.00, 1,  1500.00),
-- Order 10 (total 35000 = 18500 + 12000 + 4500)
('20000000-0000-4000-8000-000000000019', '10000000-0000-4000-8000-00000000000a', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', 'COM-MARVEL-L',       'Comedero Marvel L',                   18500.00, 1, 18500.00),
('20000000-0000-4000-8000-00000000001a', '10000000-0000-4000-8000-00000000000a', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', 'EXC-PER-ADU-3KG',    'Excellent Perro Adulto 3kg',          12000.00, 1, 12000.00),
('20000000-0000-4000-8000-00000000001b', '10000000-0000-4000-8000-00000000000a', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', 'COM-PLAS-MED',       'Comedero Plástico Mediano',            4500.00, 1,  4500.00),
-- Order 11 (total 52200 = 24500 + 18200 + 4600 + 4900)
('20000000-0000-4000-8000-00000000001c', '10000000-0000-4000-8000-00000000000b', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', 'PP-STER-GAT-3KG',    'Pro Plan Sterilized Gato 3kg',        24500.00, 1, 24500.00),
('20000000-0000-4000-8000-00000000001d', '10000000-0000-4000-8000-00000000000b', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24', 'EXC-GAT-ADU-3.5KG',  'Excellent Gato Adulto 3.5kg',         18200.00, 1, 18200.00),
('20000000-0000-4000-8000-00000000001e', '10000000-0000-4000-8000-00000000000b', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f', 'CEPILLO-DBL-M',      'Cepillo Quitapelo Doble Cara M',       4600.00, 1,  4600.00),
('20000000-0000-4000-8000-00000000001f', '10000000-0000-4000-8000-00000000000b', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36', 'ARENA-AGLO-5KG',     'Arena Sanitaria Aglomerante 5kg',      4900.00, 1,  4900.00),
-- Order 12 (total 19800 = 8500 + 4600 + 1800 + 4900)
('20000000-0000-4000-8000-000000000020', '10000000-0000-4000-8000-00000000000c', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c30', 'RASC-CARTON-CIRC',   'Rascador Cartón Corrugado Circular',   8500.00, 1,  8500.00),
('20000000-0000-4000-8000-000000000021', '10000000-0000-4000-8000-00000000000c', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3f', 'CEPILLO-DBL-M',      'Cepillo Quitapelo Doble Cara M',       4600.00, 1,  4600.00),
('20000000-0000-4000-8000-000000000022', '10000000-0000-4000-8000-00000000000c', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c3a', 'RATON-CATNIP-PACK3', 'Ratón de Tela con Catnip (3u)',        1800.00, 1,  1800.00),
('20000000-0000-4000-8000-000000000023', '10000000-0000-4000-8000-00000000000c', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c36', 'ARENA-AGLO-5KG',     'Arena Sanitaria Aglomerante 5kg',      4900.00, 1,  4900.00),
-- Order 13 (total 42000)
('20000000-0000-4000-8000-000000000024', '10000000-0000-4000-8000-00000000000d', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27', 'EXC-PER-ADU-15KG',   'Excellent Perro Adulto 15kg',         42000.00, 1, 42000.00),
-- Order 14 (total 7600 = 2500 + 3200 + 1900)
('20000000-0000-4000-8000-000000000025', '10000000-0000-4000-8000-00000000000e', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c38', 'PELOTA-GOMA-M',      'Pelota Goma Mordedor M',               2500.00, 1,  2500.00),
('20000000-0000-4000-8000-000000000026', '10000000-0000-4000-8000-00000000000e', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c39', 'PELOTA-GOMA-L',      'Pelota Goma Mordedor L',               3200.00, 1,  3200.00),
('20000000-0000-4000-8000-000000000027', '10000000-0000-4000-8000-00000000000e', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c42', 'PELOTA-PLAS-SON',    'Pelota Plástica con Sonido',           1900.00, 1,  1900.00),
-- Order 15 (total 28700 = 18500 + 10200)
('20000000-0000-4000-8000-000000000028', '10000000-0000-4000-8000-00000000000f', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', 'COM-MARVEL-L',       'Comedero Marvel L',                   18500.00, 1, 18500.00),
('20000000-0000-4000-8000-000000000029', '10000000-0000-4000-8000-00000000000f', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c35', 'CHAMPU-HIPO-1L',     'Champú Hipoalergénico 1L',            10200.00, 1, 10200.00),
-- Order 16 CANCELLED (total 15000)
('20000000-0000-4000-8000-00000000002a', '10000000-0000-4000-8000-000000000010', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'RC-AD-3KG',          'Royal Canin Adulto 3kg',              15000.00, 1, 15000.00),
-- Order 17 CANCELLED (total 4500)
('20000000-0000-4000-8000-00000000002b', '10000000-0000-4000-8000-000000000011', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', 'COM-PLAS-MED',       'Comedero Plástico Mediano',            4500.00, 1,  4500.00),
-- Order 18 CANCELLED (total 33900 = 15000 + 18900)
('20000000-0000-4000-8000-00000000002c', '10000000-0000-4000-8000-000000000012', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'RC-AD-3KG',          'Royal Canin Adulto 3kg',              15000.00, 1, 15000.00),
('20000000-0000-4000-8000-00000000002d', '10000000-0000-4000-8000-000000000012', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', 'RC-KIT-GAT-1.5KG',   'Royal Canin Kitten 1.5kg',            18900.00, 1, 18900.00),
-- Order 19 CANCELLED (total 8500)
('20000000-0000-4000-8000-00000000002e', '10000000-0000-4000-8000-000000000013', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c30', 'RASC-CARTON-CIRC',   'Rascador Cartón Corrugado Circular',   8500.00, 1,  8500.00),
-- Order 20 CANCELLED (total 24500)
('20000000-0000-4000-8000-00000000002f', '10000000-0000-4000-8000-000000000014', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', 'PP-STER-GAT-3KG',    'Pro Plan Sterilized Gato 3kg',        24500.00, 1, 24500.00);


-- =============================================================================
-- 12. ORDERS — order_status_history (1 transición por order)
-- =============================================================================
INSERT INTO orders.order_status_history (id, order_id, prev_status, new_status, reason, modified_by, created_at) VALUES
('70000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '30 days'),
('70000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '25 days'),
('70000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '22 days'),
('70000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '20 days'),
('70000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000005', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '18 days'),
('70000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000006', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '15 days'),
('70000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000007', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '14 days'),
('70000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000008', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '12 days'),
('70000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000009', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '10 days'),
('70000000-0000-4000-8000-00000000000a', '10000000-0000-4000-8000-00000000000a', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '9 days'),
('70000000-0000-4000-8000-00000000000b', '10000000-0000-4000-8000-00000000000b', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '8 days'),
('70000000-0000-4000-8000-00000000000c', '10000000-0000-4000-8000-00000000000c', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '6 days'),
('70000000-0000-4000-8000-00000000000d', '10000000-0000-4000-8000-00000000000d', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '5 days'),
('70000000-0000-4000-8000-00000000000e', '10000000-0000-4000-8000-00000000000e', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '3 days'),
('70000000-0000-4000-8000-00000000000f', '10000000-0000-4000-8000-00000000000f', 'PENDING', 'CONFIRMED', 'Pago aprobado',                NULL, NOW() - INTERVAL '2 days'),
('70000000-0000-4000-8000-000000000010', '10000000-0000-4000-8000-000000000010', 'PENDING', 'CANCELLED', 'Cliente canceló',              NULL, NOW() - INTERVAL '27 days'),
('70000000-0000-4000-8000-000000000011', '10000000-0000-4000-8000-000000000011', 'PENDING', 'CANCELLED', 'Pago rechazado',               NULL, NOW() - INTERVAL '21 days'),
('70000000-0000-4000-8000-000000000012', '10000000-0000-4000-8000-000000000012', 'PENDING', 'CANCELLED', 'Stock insuficiente',           NULL, NOW() - INTERVAL '13 days'),
('70000000-0000-4000-8000-000000000013', '10000000-0000-4000-8000-000000000013', 'PENDING', 'CANCELLED', '3 intentos de envío fallidos', NULL, NOW() - INTERVAL '7 days'),
('70000000-0000-4000-8000-000000000014', '10000000-0000-4000-8000-000000000014', 'PENDING', 'CANCELLED', 'Cliente canceló',              NULL, NOW() - INTERVAL '4 days');


-- =============================================================================
-- 13. ORDERS — Payments (1 por order; amount = orders.total)
--      CONFIRMED → PAID
--      CANCELLED → FAILED (pago rechazado) o REFUNDED (cliente canceló post pago)
-- =============================================================================
INSERT INTO orders.payments (id, session_id, order_id, user_id, provider, provider_payment_id, amount, currency, status, idempotency_key, created_at) VALUES
('30000000-0000-4000-8000-000000000001', '11111111-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', 'mercadopago', 'MP-PAY-000001', 60000.00,  'ARS', 'PAID',     'idem-000001', NOW() - INTERVAL '30 days'),
('30000000-0000-4000-8000-000000000002', '11111111-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c02', 'mercadopago', 'MP-PAY-000002', 47000.00,  'ARS', 'PAID',     'idem-000002', NOW() - INTERVAL '25 days'),
('30000000-0000-4000-8000-000000000003', '11111111-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'stripe',      'ST-PAY-000003', 36700.00,  'ARS', 'PAID',     'idem-000003', NOW() - INTERVAL '22 days'),
('30000000-0000-4000-8000-000000000004', '11111111-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'mercadopago', 'MP-PAY-000004', 110000.00, 'ARS', 'PAID',     'idem-000004', NOW() - INTERVAL '20 days'),
('30000000-0000-4000-8000-000000000005', '11111111-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000005', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'decidir',     'DC-PAY-000005', 24500.00,  'ARS', 'PAID',     'idem-000005', NOW() - INTERVAL '18 days'),
('30000000-0000-4000-8000-000000000006', '11111111-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000006', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'mercadopago', 'MP-PAY-000006', 18900.00,  'ARS', 'PAID',     'idem-000006', NOW() - INTERVAL '15 days'),
('30000000-0000-4000-8000-000000000007', '11111111-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000007', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'mercadopago', 'MP-PAY-000007', 73600.00,  'ARS', 'PAID',     'idem-000007', NOW() - INTERVAL '14 days'),
('30000000-0000-4000-8000-000000000008', '11111111-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000008', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'stripe',      'ST-PAY-000008', 9000.00,   'ARS', 'PAID',     'idem-000008', NOW() - INTERVAL '12 days'),
('30000000-0000-4000-8000-000000000009', '11111111-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000009', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'mercadopago', 'MP-PAY-000009', 14100.00,  'ARS', 'PAID',     'idem-000009', NOW() - INTERVAL '10 days'),
('30000000-0000-4000-8000-00000000000a', '11111111-0000-4000-8000-00000000000a', '10000000-0000-4000-8000-00000000000a', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0a', 'mercadopago', 'MP-PAY-000010', 35000.00,  'ARS', 'PAID',     'idem-000010', NOW() - INTERVAL '9 days'),
('30000000-0000-4000-8000-00000000000b', '11111111-0000-4000-8000-00000000000b', '10000000-0000-4000-8000-00000000000b', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', 'decidir',     'DC-PAY-000011', 52200.00,  'ARS', 'PAID',     'idem-000011', NOW() - INTERVAL '8 days'),
('30000000-0000-4000-8000-00000000000c', '11111111-0000-4000-8000-00000000000c', '10000000-0000-4000-8000-00000000000c', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0c', 'mercadopago', 'MP-PAY-000012', 19800.00,  'ARS', 'PAID',     'idem-000012', NOW() - INTERVAL '6 days'),
('30000000-0000-4000-8000-00000000000d', '11111111-0000-4000-8000-00000000000d', '10000000-0000-4000-8000-00000000000d', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', 'mercadopago', 'MP-PAY-000013', 42000.00,  'ARS', 'PAID',     'idem-000013', NOW() - INTERVAL '5 days'),
('30000000-0000-4000-8000-00000000000e', '11111111-0000-4000-8000-00000000000e', '10000000-0000-4000-8000-00000000000e', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0e', 'stripe',      'ST-PAY-000014', 7600.00,   'ARS', 'PAID',     'idem-000014', NOW() - INTERVAL '3 days'),
('30000000-0000-4000-8000-00000000000f', '11111111-0000-4000-8000-00000000000f', '10000000-0000-4000-8000-00000000000f', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0f', 'mercadopago', 'MP-PAY-000015', 28700.00,  'ARS', 'PAID',     'idem-000015', NOW() - INTERVAL '2 days'),
('30000000-0000-4000-8000-000000000010', '11111111-0000-4000-8000-000000000010', '10000000-0000-4000-8000-000000000010', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', 'mercadopago', 'MP-PAY-000016', 15000.00,  'ARS', 'REFUNDED', 'idem-000016', NOW() - INTERVAL '27 days'),
('30000000-0000-4000-8000-000000000011', '11111111-0000-4000-8000-000000000011', '10000000-0000-4000-8000-000000000011', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'stripe',      'ST-PAY-000017', 4500.00,   'ARS', 'FAILED',   'idem-000017', NOW() - INTERVAL '21 days'),
('30000000-0000-4000-8000-000000000012', '11111111-0000-4000-8000-000000000012', '10000000-0000-4000-8000-000000000012', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'mercadopago', 'MP-PAY-000018', 33900.00,  'ARS', 'FAILED',   'idem-000018', NOW() - INTERVAL '13 days'),
('30000000-0000-4000-8000-000000000013', '11111111-0000-4000-8000-000000000013', '10000000-0000-4000-8000-000000000013', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0b', 'mercadopago', 'MP-PAY-000019', 8500.00,   'ARS', 'REFUNDED', 'idem-000019', NOW() - INTERVAL '7 days'),
('30000000-0000-4000-8000-000000000014', '11111111-0000-4000-8000-000000000014', '10000000-0000-4000-8000-000000000014', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380c0d', 'fake',        'FK-PAY-000020', 24500.00,  'ARS', 'FAILED',   'idem-000020', NOW() - INTERVAL '4 days');


-- =============================================================================
-- 14. ORDERS — payment_status_history (PENDING/PAID → estado final)
-- =============================================================================
INSERT INTO orders.payment_status_history (id, payment_id, prev_status, new_status, created_at) VALUES
('80000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', 'PENDING', 'PAID',     NOW() - INTERVAL '30 days'),
('80000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000002', 'PENDING', 'PAID',     NOW() - INTERVAL '25 days'),
('80000000-0000-4000-8000-000000000003', '30000000-0000-4000-8000-000000000003', 'PENDING', 'PAID',     NOW() - INTERVAL '22 days'),
('80000000-0000-4000-8000-000000000004', '30000000-0000-4000-8000-000000000004', 'PENDING', 'PAID',     NOW() - INTERVAL '20 days'),
('80000000-0000-4000-8000-000000000005', '30000000-0000-4000-8000-000000000005', 'PENDING', 'PAID',     NOW() - INTERVAL '18 days'),
('80000000-0000-4000-8000-000000000006', '30000000-0000-4000-8000-000000000006', 'PENDING', 'PAID',     NOW() - INTERVAL '15 days'),
('80000000-0000-4000-8000-000000000007', '30000000-0000-4000-8000-000000000007', 'PENDING', 'PAID',     NOW() - INTERVAL '14 days'),
('80000000-0000-4000-8000-000000000008', '30000000-0000-4000-8000-000000000008', 'PENDING', 'PAID',     NOW() - INTERVAL '12 days'),
('80000000-0000-4000-8000-000000000009', '30000000-0000-4000-8000-000000000009', 'PENDING', 'PAID',     NOW() - INTERVAL '10 days'),
('80000000-0000-4000-8000-00000000000a', '30000000-0000-4000-8000-00000000000a', 'PENDING', 'PAID',     NOW() - INTERVAL '9 days'),
('80000000-0000-4000-8000-00000000000b', '30000000-0000-4000-8000-00000000000b', 'PENDING', 'PAID',     NOW() - INTERVAL '8 days'),
('80000000-0000-4000-8000-00000000000c', '30000000-0000-4000-8000-00000000000c', 'PENDING', 'PAID',     NOW() - INTERVAL '6 days'),
('80000000-0000-4000-8000-00000000000d', '30000000-0000-4000-8000-00000000000d', 'PENDING', 'PAID',     NOW() - INTERVAL '5 days'),
('80000000-0000-4000-8000-00000000000e', '30000000-0000-4000-8000-00000000000e', 'PENDING', 'PAID',     NOW() - INTERVAL '3 days'),
('80000000-0000-4000-8000-00000000000f', '30000000-0000-4000-8000-00000000000f', 'PENDING', 'PAID',     NOW() - INTERVAL '2 days'),
('80000000-0000-4000-8000-000000000010', '30000000-0000-4000-8000-000000000010', 'PAID',    'REFUNDED', NOW() - INTERVAL '26 days'),
('80000000-0000-4000-8000-000000000011', '30000000-0000-4000-8000-000000000011', 'PENDING', 'FAILED',   NOW() - INTERVAL '21 days'),
('80000000-0000-4000-8000-000000000012', '30000000-0000-4000-8000-000000000012', 'PENDING', 'FAILED',   NOW() - INTERVAL '13 days'),
('80000000-0000-4000-8000-000000000013', '30000000-0000-4000-8000-000000000013', 'PAID',    'REFUNDED', NOW() - INTERVAL '6 days'),
('80000000-0000-4000-8000-000000000014', '30000000-0000-4000-8000-000000000014', 'PENDING', 'FAILED',   NOW() - INTERVAL '4 days');


-- =============================================================================
-- 15. SHIPMENTS — Envíos (uno por CONFIRMED order)
--      Distribución por estado: 5 DELIVERED + 4 IN_TRANSIT + 3 PREPARED + 2 CONFIRMED + 1 CANCELLED.
-- =============================================================================
INSERT INTO shipments.shipments (id, order_id, warehouse_id, courier, status, attempts, last_attempt_at, created_at, updated_at) VALUES
('40000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 1, 'OWN_DELIVERY', 'DELIVERED',  1, NOW() - INTERVAL '28 days', NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days'),
('40000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 1, 'ANDREANI',     'DELIVERED',  1, NOW() - INTERVAL '23 days', NOW() - INTERVAL '25 days', NOW() - INTERVAL '23 days'),
('40000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 2, 'OCA',          'DELIVERED',  1, NOW() - INTERVAL '20 days', NOW() - INTERVAL '22 days', NOW() - INTERVAL '20 days'),
('40000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 3, 'CORREO_ARG',   'DELIVERED',  2, NOW() - INTERVAL '18 days', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days'),
('40000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000005', 1, 'OWN_DELIVERY', 'DELIVERED',  1, NOW() - INTERVAL '16 days', NOW() - INTERVAL '18 days', NOW() - INTERVAL '16 days'),
('40000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000006', 2, 'ANDREANI',     'IN_TRANSIT', 1, NOW() - INTERVAL '13 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '13 days'),
('40000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000007', 1, 'OWN_DELIVERY', 'IN_TRANSIT', 1, NOW() - INTERVAL '12 days', NOW() - INTERVAL '14 days', NOW() - INTERVAL '12 days'),
('40000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000008', 3, 'OCA',          'IN_TRANSIT', 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '12 days', NOW() - INTERVAL '10 days'),
('40000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000009', 2, 'ANDREANI',     'IN_TRANSIT', 1, NOW() - INTERVAL '8 days',  NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days'),
('40000000-0000-4000-8000-00000000000a', '10000000-0000-4000-8000-00000000000a', 1, 'OWN_DELIVERY', 'PREPARED',   0, NULL,                       NOW() - INTERVAL '9 days',  NOW() - INTERVAL '8 days'),
('40000000-0000-4000-8000-00000000000b', '10000000-0000-4000-8000-00000000000b', 2, 'CORREO_ARG',   'PREPARED',   0, NULL,                       NOW() - INTERVAL '8 days',  NOW() - INTERVAL '7 days'),
('40000000-0000-4000-8000-00000000000c', '10000000-0000-4000-8000-00000000000c', 3, 'OCA',          'PREPARED',   0, NULL,                       NOW() - INTERVAL '6 days',  NOW() - INTERVAL '5 days'),
('40000000-0000-4000-8000-00000000000d', '10000000-0000-4000-8000-00000000000d', 1, 'OWN_DELIVERY', 'CONFIRMED',  0, NULL,                       NOW() - INTERVAL '5 days',  NOW() - INTERVAL '5 days'),
('40000000-0000-4000-8000-00000000000e', '10000000-0000-4000-8000-00000000000e', 2, 'ANDREANI',     'CONFIRMED',  0, NULL,                       NOW() - INTERVAL '3 days',  NOW() - INTERVAL '3 days'),
('40000000-0000-4000-8000-00000000000f', '10000000-0000-4000-8000-00000000000f', 1, 'OWN_DELIVERY', 'CANCELLED',  0, NULL,                       NOW() - INTERVAL '2 days',  NOW() - INTERVAL '1 day');


-- =============================================================================
-- 16. SHIPMENTS — shipment_status (transiciones por shipment)
-- =============================================================================
INSERT INTO shipments.shipment_status (id, shipment_id, prev_status, new_status, reason, modified_by, created_at) VALUES
-- DELIVERED: CONFIRMED → PREPARED → IN_TRANSIT → DELIVERED
('90000000-0000-4000-8000-000000000001', '40000000-0000-4000-8000-000000000001', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '29 days'),
('90000000-0000-4000-8000-000000000002', '40000000-0000-4000-8000-000000000001', 'PREPARED',   'IN_TRANSIT', 'Despachado',            'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '29 days'),
('90000000-0000-4000-8000-000000000003', '40000000-0000-4000-8000-000000000001', 'IN_TRANSIT', 'DELIVERED',  'Entregado al cliente',  'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '28 days'),

('90000000-0000-4000-8000-000000000004', '40000000-0000-4000-8000-000000000002', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '24 days'),
('90000000-0000-4000-8000-000000000005', '40000000-0000-4000-8000-000000000002', 'PREPARED',   'IN_TRANSIT', 'Despachado Andreani',   'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '24 days'),
('90000000-0000-4000-8000-000000000006', '40000000-0000-4000-8000-000000000002', 'IN_TRANSIT', 'DELIVERED',  'Entregado al cliente',  NULL,                                   NOW() - INTERVAL '23 days'),

('90000000-0000-4000-8000-000000000007', '40000000-0000-4000-8000-000000000003', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '21 days'),
('90000000-0000-4000-8000-000000000008', '40000000-0000-4000-8000-000000000003', 'PREPARED',   'IN_TRANSIT', 'Despachado OCA',        'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '21 days'),
('90000000-0000-4000-8000-000000000009', '40000000-0000-4000-8000-000000000003', 'IN_TRANSIT', 'DELIVERED',  'Entregado al cliente',  NULL,                                   NOW() - INTERVAL '20 days'),

('90000000-0000-4000-8000-00000000000a', '40000000-0000-4000-8000-000000000004', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '19 days'),
('90000000-0000-4000-8000-00000000000b', '40000000-0000-4000-8000-000000000004', 'PREPARED',   'IN_TRANSIT', 'Despachado Correo Arg', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '19 days'),
('90000000-0000-4000-8000-00000000000c', '40000000-0000-4000-8000-000000000004', 'IN_TRANSIT', 'DELIVERED',  'Entregado al cliente',  NULL,                                   NOW() - INTERVAL '18 days'),

('90000000-0000-4000-8000-00000000000d', '40000000-0000-4000-8000-000000000005', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '17 days'),
('90000000-0000-4000-8000-00000000000e', '40000000-0000-4000-8000-000000000005', 'PREPARED',   'IN_TRANSIT', 'Despachado',            'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '17 days'),
('90000000-0000-4000-8000-00000000000f', '40000000-0000-4000-8000-000000000005', 'IN_TRANSIT', 'DELIVERED',  'Entregado al cliente',  'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '16 days'),

-- IN_TRANSIT: CONFIRMED → PREPARED → IN_TRANSIT
('90000000-0000-4000-8000-000000000010', '40000000-0000-4000-8000-000000000006', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '14 days'),
('90000000-0000-4000-8000-000000000011', '40000000-0000-4000-8000-000000000006', 'PREPARED',   'IN_TRANSIT', 'Despachado Andreani',   'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '13 days'),

('90000000-0000-4000-8000-000000000012', '40000000-0000-4000-8000-000000000007', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '13 days'),
('90000000-0000-4000-8000-000000000013', '40000000-0000-4000-8000-000000000007', 'PREPARED',   'IN_TRANSIT', 'Despachado',            'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '12 days'),

('90000000-0000-4000-8000-000000000014', '40000000-0000-4000-8000-000000000008', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '11 days'),
('90000000-0000-4000-8000-000000000015', '40000000-0000-4000-8000-000000000008', 'PREPARED',   'IN_TRANSIT', 'Despachado OCA',        'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '10 days'),

('90000000-0000-4000-8000-000000000016', '40000000-0000-4000-8000-000000000009', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '9 days'),
('90000000-0000-4000-8000-000000000017', '40000000-0000-4000-8000-000000000009', 'PREPARED',   'IN_TRANSIT', 'Despachado Andreani',   'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '8 days'),

-- PREPARED: CONFIRMED → PREPARED
('90000000-0000-4000-8000-000000000018', '40000000-0000-4000-8000-00000000000a', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', NOW() - INTERVAL '8 days'),
('90000000-0000-4000-8000-000000000019', '40000000-0000-4000-8000-00000000000b', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', NOW() - INTERVAL '7 days'),
('90000000-0000-4000-8000-00000000001a', '40000000-0000-4000-8000-00000000000c', 'CONFIRMED',  'PREPARED',   'Pedido empaquetado',    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', NOW() - INTERVAL '5 days'),

-- CANCELLED: CONFIRMED → CANCELLED
('90000000-0000-4000-8000-00000000001b', '40000000-0000-4000-8000-00000000000f', 'CONFIRMED',  'CANCELLED',  'Devolución solicitada', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e01', NOW() - INTERVAL '1 day');


-- =============================================================================
-- 17. SHIPMENTS — shipment_attempts (alineados con shipments.attempts)
-- =============================================================================
INSERT INTO shipments.shipment_attempts (id, shipment_id, attempt_number, result, observation, registered_at) VALUES
('60000000-0000-4000-8000-000000000001', '40000000-0000-4000-8000-000000000001', 1, 'SUCCESS', 'Entrega exitosa',                    NOW() - INTERVAL '28 days'),
('60000000-0000-4000-8000-000000000002', '40000000-0000-4000-8000-000000000002', 1, 'SUCCESS', 'Entrega exitosa',                    NOW() - INTERVAL '23 days'),
('60000000-0000-4000-8000-000000000003', '40000000-0000-4000-8000-000000000003', 1, 'SUCCESS', 'Entrega exitosa',                    NOW() - INTERVAL '20 days'),
('60000000-0000-4000-8000-000000000004', '40000000-0000-4000-8000-000000000004', 1, 'FAILED',  'Cliente ausente, se reagenda',       NOW() - INTERVAL '19 days'),
('60000000-0000-4000-8000-000000000005', '40000000-0000-4000-8000-000000000004', 2, 'SUCCESS', 'Entrega exitosa en segundo intento', NOW() - INTERVAL '18 days'),
('60000000-0000-4000-8000-000000000006', '40000000-0000-4000-8000-000000000005', 1, 'SUCCESS', 'Entrega exitosa',                    NOW() - INTERVAL '16 days'),
('60000000-0000-4000-8000-000000000007', '40000000-0000-4000-8000-000000000006', 1, 'SUCCESS', 'Despacho a Andreani confirmado',     NOW() - INTERVAL '13 days'),
('60000000-0000-4000-8000-000000000008', '40000000-0000-4000-8000-000000000007', 1, 'SUCCESS', 'Despacho confirmado',                NOW() - INTERVAL '12 days'),
('60000000-0000-4000-8000-000000000009', '40000000-0000-4000-8000-000000000008', 1, 'SUCCESS', 'Despacho a OCA confirmado',          NOW() - INTERVAL '10 days'),
('60000000-0000-4000-8000-00000000000a', '40000000-0000-4000-8000-000000000009', 1, 'SUCCESS', 'Despacho a Andreani confirmado',     NOW() - INTERVAL '8 days');


-- =============================================================================
-- 18. SHIPMENTS — order_assignments
--      DELIVERED → COMPLETED (con completed_at)
--      IN_TRANSIT / PREPARED → IN_PROGRESS
--      CONFIRMED y CANCELLED quedan sin asignación (aún no se preparó)
-- =============================================================================
INSERT INTO shipments.order_assignments (id, order_id, warehouse_id, operator_id, status, assigned_at, updated_at, completed_at) VALUES
('50000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 1, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'COMPLETED',   NOW() - INTERVAL '29 days', NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),
('50000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 1, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', 'COMPLETED',   NOW() - INTERVAL '24 days', NOW() - INTERVAL '23 days', NOW() - INTERVAL '23 days'),
('50000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 2, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', 'COMPLETED',   NOW() - INTERVAL '21 days', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
('50000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 3, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', 'COMPLETED',   NOW() - INTERVAL '19 days', NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
('50000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000005', 1, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'COMPLETED',   NOW() - INTERVAL '17 days', NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
('50000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000006', 2, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', 'IN_PROGRESS', NOW() - INTERVAL '14 days', NOW() - INTERVAL '13 days', NULL),
('50000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000007', 1, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'IN_PROGRESS', NOW() - INTERVAL '13 days', NOW() - INTERVAL '12 days', NULL),
('50000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000008', 3, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', 'IN_PROGRESS', NOW() - INTERVAL '11 days', NOW() - INTERVAL '10 days', NULL),
('50000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000009', 2, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', 'IN_PROGRESS', NOW() - INTERVAL '9 days',  NOW() - INTERVAL '8 days',  NULL),
('50000000-0000-4000-8000-00000000000a', '10000000-0000-4000-8000-00000000000a', 1, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'IN_PROGRESS', NOW() - INTERVAL '8 days',  NOW() - INTERVAL '8 days',  NULL),
('50000000-0000-4000-8000-00000000000b', '10000000-0000-4000-8000-00000000000b', 2, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12', 'IN_PROGRESS', NOW() - INTERVAL '7 days',  NOW() - INTERVAL '7 days',  NULL),
('50000000-0000-4000-8000-00000000000c', '10000000-0000-4000-8000-00000000000c', 3, 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13', 'IN_PROGRESS', NOW() - INTERVAL '5 days',  NOW() - INTERVAL '5 days',  NULL);
