-- =============================================================================
-- SCHEMA: catalog
--    Módulo propietario: com.virtualpet.modulo.catalog
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS catalog;

-- =============================================================================
-- TABLE: categories
-- =============================================================================
CREATE TABLE catalog.categories (
     id          UUID            NOT NULL DEFAULT gen_random_uuid(),
     name        VARCHAR(100)    NOT NULL,
     slug        VARCHAR(100)    NOT NULL,
     father_id   UUID,
     active      BOOLEAN         NOT NULL DEFAULT TRUE,

     CONSTRAINT pk_categories PRIMARY KEY (id),
     CONSTRAINT fk_categories FOREIGN KEY (father_id) REFERENCES catalog.categories(id) ON DELETE RESTRICT,
     CONSTRAINT uq_categoria_slug UNIQUE (slug),
     CONSTRAINT chk_categoria_no_self CHECK (id <> father_id)
);

-- =============================================================================
-- TABLE: products
--      Producto padre. Las variantes con SKU están en variante_sku
--      Columna search_vecotor generada para full-text search.
--      Actualizacion automatica al modificar nombre/descripcion/marca
--      Postgres Generated Always As requiere que la expression sea INMUTABLE. Unnaccent es STABLE, no inmutable.
--      Definimos la columna como GENERATED ALWAYS AS y usamos un trigger para actualizarla en cada cambio relevante.
-- =============================================================================
CREATE TABLE catalog.products (
       id              UUID            NOT NULL DEFAULT gen_random_uuid(),
       name            VARCHAR(255)    NOT NULL,
       description     TEXT,
       brand           VARCHAR(100),
       category_id     UUID            NOT NULL,
       active          BOOLEAN         NOT NULL DEFAULT TRUE,
       created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
       updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

       search_vector   TSVECTOR,

       CONSTRAINT pk_id_product PRIMARY KEY (id),
       CONSTRAINT fk_id_category FOREIGN KEY (category_id) REFERENCES catalog.categories(id) ON DELETE RESTRICT
);

CREATE FUNCTION catalog.products_search_vector_update()
RETURNS trigger AS $$
BEGIN
  NEW.search_vector :=
    to_tsvector(
      'spanish',
      unaccent(NEW.name) || ' ' ||
      unaccent(COALESCE(NEW.description, '')) || ' ' ||
      unaccent(COALESCE(NEW.brand, ''))
    );

  RETURN NEW;
END
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_products_search_vector
BEFORE INSERT OR UPDATE
ON catalog.products
FOR EACH ROW
EXECUTE FUNCTION catalog.products_search_vector_update();

-- =============================================================================
-- TABLE: product_variants
--      SKU: unidad minima vendible. El campo atributos JSONB soporta cualquier combinacion (color, talle, peso, etc.)
--      Ejemplo: JSON {"color": "rojo", "peso": "5kg"}. Indexado con GIN para filtros por atributo
--      El STOCK se modifica SOLO en transacciones del modulo checkout. El backoffice no lo toca
--      Incluye campo 'version' para manejo de concurrencia optimista con JPA.
-- =============================================================================
CREATE TABLE catalog.product_variants (
       id                  UUID               NOT NULL DEFAULT gen_random_uuid(),
       product_id          UUID               NOT NULL,
       sku                 VARCHAR(100)       NOT NULL,
       attributes          JSONB              NOT NULL DEFAULT '{}',
       price               NUMERIC(10,2)      NOT NULL,
       stock               INT                NOT NULL DEFAULT 0,
       stock_min           INT                NOT NULL DEFAULT 0,
       image_url           VARCHAR(500)       ,
       version             BIGINT             NOT NULL DEFAULT 0,
       active              BOOLEAN            NOT NULL DEFAULT TRUE,
       created_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_product_variant PRIMARY KEY (id),
       CONSTRAINT fk_product_id FOREIGN KEY (product_id) REFERENCES catalog.products(id) ON DELETE RESTRICT,
       CONSTRAINT chk_positive_price CHECK (price > 0),
       CONSTRAINT chk_no_negative_stock CHECK (stock >= 0),
       CONSTRAINT chk_minimus_stock CHECK (stock_min >= 0),
       CONSTRAINT uq_product_variant UNIQUE (sku)
);

-- =============================================================================
-- TABLE: image_variants
-- =============================================================================
CREATE TABLE catalog.image_variants (
     id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
     product_variant_id  UUID            NOT NULL,
     url_s3              VARCHAR(500)    NOT NULL,
     is_main             BOOLEAN         NOT NULL DEFAULT FALSE,
     order_img           SMALLINT        NOT NULL DEFAULT 0,
     created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

     CONSTRAINT pk_image_variant_id PRIMARY KEY (id),
     CONSTRAINT fk_product_variant_id FOREIGN KEY (product_variant_id) REFERENCES catalog.product_variants(id) ON DELETE CASCADE,
     CONSTRAINT chk_img_url CHECK (url_s3 ~ '^https://')
);

-- =============================================================================
-- INDICES
-- =============================================================================
CREATE INDEX idx_products_category ON catalog.products (category_id);
CREATE INDEX idx_products_fts ON catalog.products USING GIN(search_vector);

CREATE INDEX idx_variants_product_id ON catalog.product_variants (product_id);
CREATE INDEX idx_variants_attributes ON catalog.product_variants USING GIN(attributes);
CREATE INDEX idx_variants_price ON catalog.product_variants (price);
CREATE INDEX idx_variants_active ON catalog.product_variants (active, stock) WHERE active = TRUE;

CREATE INDEX idx_img_order ON catalog.image_variants (product_variant_id, order_img);
CREATE UNIQUE INDEX uq_img_main ON catalog.image_variants (product_variant_id) WHERE is_main = TRUE;