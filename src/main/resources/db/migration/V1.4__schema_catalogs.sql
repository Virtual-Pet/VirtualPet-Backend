-- 1. Renombrar columnas (para que ambas tablas usen image_path y sean 100% agnósticas)
ALTER TABLE catalog.image_variants RENAME COLUMN url_s3 TO image_path;
ALTER TABLE catalog.product_variants RENAME COLUMN image_url TO image_path;

-- 2. Eliminar la restricción vieja y crear la nueva
ALTER TABLE catalog.image_variants DROP CONSTRAINT chk_img_url;
ALTER TABLE catalog.image_variants ADD CONSTRAINT chk_img_path CHECK (image_path ~ '^/');

-- =============================================================================
-- 3. SCHEMA: catalogo — Categorías
-- =============================================================================
INSERT INTO catalog.categories (id, name, slug, father_id, active) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alimentos', 'alimentos', NULL, TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Alimentos para Perros', 'alimentos-perros', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Alimentos para Gatos', 'alimentos-gatos', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Accesorios', 'accesorios', NULL, TRUE);

-- =============================================================================
-- 4. PRODUCTOS
-- =============================================================================
INSERT INTO catalog.products (id, name, description, brand, pet_type, category_id, active) VALUES
-- Alimentos para perro
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'Royal Canin Adulto', 'Alimento balanceado premium para perros adultos.', 'Royal Canin', 'Perro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE),

-- Alimentos para gatos
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'Pro Plan Sterilized Gato', 'Alimento premium formulado para el control de peso y salud renal en gatos castrados.', 'Purina Pro Plan', 'Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14', 'Royal Canin Kitten', 'Nutrición específica para gatitos en su primera etapa de crecimiento (hasta 12 meses).', 'Royal Canin', 'Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15', 'Excellent Gato Adulto Pollo y Arroz', 'Alimento completo y balanceado de alta digestibilidad con sabor a pollo y arroz.', 'Purina Excellent', 'Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', TRUE),

-- Comedero Plástico
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16', 'Comedero Plástico Mediano', 'Comedero clásico de plástico resistente, base antideslizante y fácil de lavar.', 'Nacional', 'Perro/Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE),

-- Comedero Marvel L
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17', 'Comedero Marvel L', 'Comedero original con diseño exclusivo de Marvel. Tamaño grande (L), cuenta con plato de acero inoxidable desmontable y base de melamina antideslizante.', 'Marvel', 'Perro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE),

-- Cuchas para Perros
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18', 'Cucha Térmica Plástica', 'Cucha de exterior fabricada en plástico inyectado con protección UV. Aislante térmico, fácil de lavar y no junta pulgas.', 'PetHouse', 'Perro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19', 'Cucha de Madera Techo a Dos Aguas', 'Cucha artesanal de madera de pino tratada para exterior. Techo de chapa a dos aguas para evitar la acumulación de lluvia.', 'Nacional', 'Perro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE);

-- =============================================================================
-- 5. VARIANTES DE PRODUCTO
-- =============================================================================
INSERT INTO catalog.product_variants (id, product_id, sku, attributes, price, stock, stock_min, image_path, active) VALUES
-- Variantes del Alimento de Perro
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'RC-AD-3KG', '{"peso": "3kg"}', 15000.00, 50, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'RC-AD-15KG', '{"peso": "15kg"}', 45000.00, 20, 2, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/1.jpg', TRUE),

-- Variantes de Alimentos para Gatos 
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'PP-STER-GAT-3KG', '{"peso": "3kg"}', 24500.00, 30, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14', 'RC-KIT-GAT-1.5KG', '{"peso": "1.5kg"}', 18900.00, 15, 4, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15', 'EXC-ADU-GAT-7.5KG', '{"peso": "7.5kg"}', 32000.00, 25, 6, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/1.jpg', TRUE),

-- Variante del Comedero
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16', 'COM-PLAS-MED', '{"talle": "Mediano", "material": "Plástico", "color": "Surtido"}', 4500.00, 100, 10, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16/1.jpg', TRUE),

-- Variante del Comedero Marvel
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17', 'COM-MARVEL-L', '{"talle": "L", "material": "Melamina y Acero", "licencia": "Marvel"}', 18500.00, 25, 5, '/AQUI_TU_URL_COMEDERO_MARVEL.jpg', TRUE),

-- Variante Cucha Plástica
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18', 'CUCHA-PLAS-XL', '{"talle": "XL", "material": "Plástico Inyectado", "color": "Gris y Azul"}', 85000.00, 15, 2, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18/1.jpg', TRUE),

-- Variante Cucha de Madera
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19', 'CUCHA-MAD-L', '{"talle": "L", "material": "Madera y Chapa", "color": "Madera Natural"}', 110000.00, 8, 1, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19/1.jpg', TRUE);
-- =============================================================================
-- 6. IMÁGENES DE LAS VARIANTES (Corregido con IDs únicos y sintaxis perfecta)
-- =============================================================================
INSERT INTO catalog.image_variants (id, product_variant_id, image_path, is_main, order_img) VALUES
-- Perros 
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/3.jpg', FALSE, 3),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/4.jpg', FALSE, 4),

-- Gatos (Con IDs únicos)
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380013', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380014', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380015', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380016', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/3.jpg', FALSE, 3),

-- Imagen del Comedero (Ruta corregida con /productos)
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380017', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16/1.jpg', TRUE, 1),

-- Imágenes del Comedero Marvel (Con IDs únicos y comas al final)
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380018', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380019', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380020', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17/3.jpg', FALSE, 3),

-- Imágenes Cucha Plástica
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380021', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380022', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18/2.jpg', FALSE, 2),

-- Imagen Cucha de Madera
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380023', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19/1.jpg', TRUE, 1);
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380024', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19/2.jpg', FALSE, 2);