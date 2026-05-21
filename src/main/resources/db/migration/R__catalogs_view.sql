-- =============================================================================
-- Virtual Pet MDQ — VISTAS (Migraciones Repetibles R__)
-- PostgreSQL 15 - Esquema: catalog
-- =============================================================================

-- =============================================================================
-- VISTA: low_stock_variants (Variantes con Stock Bajo)
-- Descripción: Vista de conveniencia para el Backoffice. Cruza las variantes 
-- con el producto padre y filtra aquellas cuyo stock físico haya perforado 
-- el umbral de seguridad (stock_min), permitiendo alertas de reabastecimiento.
-- =============================================================================
CREATE OR REPLACE VIEW catalog.low_stock_variants AS
SELECT
    v.id,
    v.sku,
    p.name AS product_name,
    v.stock,
    v.stock_min
FROM catalog.product_variants v
    JOIN catalog.products p ON p.id = v.product_id
WHERE v.active = TRUE
  AND v.stock <= v.stock_min;