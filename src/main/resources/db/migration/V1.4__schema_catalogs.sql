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
-- Producto Padre: Purina Excellent Perro
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21', 'Excellent Perro Adulto Pollo y Arroz', 'Alimento completo y balanceado para perros adultos. Con proteínas de alta digestibilidad, calcio y antioxidantes para mantener músculos fuertes y sanos.', 'Purina Excellent', 'Perro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE),

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
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19', 'Cucha de Madera Techo a Dos Aguas', 'Cucha artesanal de madera de pino tratada para exterior. Techo de chapa a dos aguas para evitar la acumulación de lluvia.', 'Nacional', 'Perro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE),

-- Rascadores para Gatos
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'Rascador Torre 3 Niveles con Cucha', 'Rascador de madera tapizado en peluche suave con columnas de hilo sisal. Incluye cucha en la base y juguete colgante. Ideal para trepar y afilar uñas.', 'CatPlay', 'Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b23', 'Rascador Cartón Corrugado Circular', 'Rascador ecológico de cartón de alta densidad con forma circular. Incluye sobrecito de catnip para estimular el instinto del gato y evitar que arañe los muebles.', 'EcoCat', 'Gato', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', TRUE);

-- =============================================================================
-- 5. VARIANTES DE PRODUCTO
-- =============================================================================
INSERT INTO catalog.product_variants (id, product_id, sku, attributes, price, stock, stock_min, image_path, active) VALUES
-- Variantes del Alimento de Perro
-- Royal Canin Adulto 3kg y 15kg 
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'RC-AD-3KG', '{"peso": "3kg"}', 15000.00, 50, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'RC-AD-15KG', '{"peso": "15kg"}', 45000.00, 20, 2, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/1.jpg', TRUE),

-- Variante 1: Bolsa 20 kg
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21', 'EXC-PER-ADU-20KG', '{"peso": "20kg", "etapa": "Adulto"}', 55000.00, 10, 2, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26/1.jpg', TRUE),
-- Variante 2: Bolsa 15 kg
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21', 'EXC-PER-ADU-15KG', '{"peso": "15kg", "etapa": "Adulto"}', 42000.00, 15, 3, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27/1.jpg', TRUE),
-- Variante 3: Bolsa 3 kg
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21', 'EXC-PER-ADU-3KG', '{"peso": "3kg", "etapa": "Adulto"}', 12000.00, 30, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28/1.jpg', TRUE),


-- Excellent Gato Pollo y Arroz 1.5kg, 3kg y 7.5kg
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15', 'EXC-ADU-GAT-7.5KG', '{"peso": "7.5kg"}', 32000.00, 25, 6, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15', 'EXC-GAT-ADU-3.5KG', '{"peso": "3.5kg", "etapa": "Adulto"}', 18200.00, 25, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15', 'EXC-GAT-ADU-1KG', '{"peso": "1kg", "etapa": "Adulto"}', 6800.00, 40, 10, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25/1.jpg', TRUE),

-- Variantes de Alimentos para Gatos 
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'PP-STER-GAT-3KG', '{"peso": "3kg"}', 24500.00, 30, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/1.jpg', TRUE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14', 'RC-KIT-GAT-1.5KG', '{"peso": "1.5kg"}', 18900.00, 15, 4, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/1.jpg', TRUE),

-- Variante del Comedero
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16', 'COM-PLAS-MED', '{"talle": "Mediano", "material": "Plástico", "color": "Surtido"}', 4500.00, 100, 10, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16/1.jpg', TRUE),

-- Variante del Comedero Marvel
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17', 'COM-MARVEL-L', '{"talle": "L", "material": "Melamina y Acero", "licencia": "Marvel"}', 18500.00, 25, 5, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16/1.jpg', TRUE),

-- Variante Cucha Plástica
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18', 'CUCHA-PLAS-XL', '{"talle": "XL", "material": "Plástico Inyectado", "color": "Gris y Azul"}', 85000.00, 15, 2, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18/1.jpg', TRUE),

-- Variante Cucha de Madera
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19', 'CUCHA-MAD-L', '{"talle": "L", "material": "Madera y Chapa", "color": "Madera Natural"}', 110000.00, 8, 1, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19/1.jpg', TRUE),

-- Variante: Torre 3 Niveles
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c29', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'RASC-TORRE-120CM', '{"tamaño": "120 cm alto", "material": "Sisal y Peluche", "color": "Beige"}', 65000.00, 8, 2, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c29/1.jpg', TRUE),
-- Variante: Rascador de Cartón
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c30', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b23', 'RASC-CARTON-CIRC', '{"tamaño": "40 cm diámetro", "material": "Cartón Corrugado", "extra": "Con Catnip"}', 8500.00, 45, 10, '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b23/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c30/1.jpg', TRUE);

-- =============================================================================
-- 6. IMÁGENES DE LAS VARIANTES (UUIDs únicos y rutas alineadas)
-- =============================================================================
INSERT INTO catalog.image_variants (id, product_variant_id, image_path, is_main, order_img) VALUES
-- Perros: Royal Canin Adulto
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/3.jpg', FALSE, 3),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12/4.jpg', FALSE, 4),

-- Gatos: Pro Plan y Royal Canin Kitten
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c31/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380013', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c32/3.jpg', FALSE, 3),

-- Gatos: Excellent 7.5kg, 3.5kg y 1kg
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380014', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380015', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380016', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380017', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380018', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380019', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c24/3.jpg', FALSE, 3),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380020', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380021', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380022', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b15/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c25/3.jpg', FALSE, 3),

-- Comederos (Plástico y Marvel)
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380023', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b16/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c16/1.jpg', TRUE, 1),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380024', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380025', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380026', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b17/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c17/3.jpg', FALSE, 3),

-- Cuchas
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380027', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380028', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b18/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c18/2.jpg', FALSE, 2),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380029', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380030', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b19/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c19/2.jpg', FALSE, 2),

-- Rascadores
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380031', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c29', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c29/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380032', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c30', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b23/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c30/1.jpg', TRUE, 1),

-- Perros: Excellent Perro 20kg, 15kg y 3kg
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380033', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380034', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380035', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26/3.jpg', FALSE, 3),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380036', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c26/4.jpg', FALSE, 4),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380037', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380038', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380039', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27/3.jpg', FALSE, 3),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380040', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c27/4.jpg', FALSE, 4),

('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380041', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28/1.jpg', TRUE, 1),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380042', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28/2.jpg', FALSE, 2),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380043', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28/3.jpg', FALSE, 3),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380044', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28', '/productos/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21/c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c28/4.jpg', FALSE, 4);