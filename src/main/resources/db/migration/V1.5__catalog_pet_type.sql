-- =============================================================================
-- Adds pet_type to catalog.products so /products can filter and respond per the
-- OpenAPI contract. Nullable for backward compatibility with seed data; the
-- application defaults it to NULL until the product is enriched.
-- =============================================================================

ALTER TABLE catalog.products
  ADD COLUMN pet_type VARCHAR(50);

CREATE INDEX idx_products_pet_type ON catalog.products (pet_type);
