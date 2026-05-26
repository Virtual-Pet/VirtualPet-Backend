-- =============================================================================
-- Virtual Pet MDQ — FUNCIONES, TRIGGERS Y VISTAS (Migraciones Repetibles R__)
-- PostgreSQL 15 - Esquema: catalog
-- =============================================================================

-- =============================================================================
-- FUNCION: Actualización automática de timestamp (Auditoría)
-- Descripción: Función propia del módulo de catálogo para mantener su
-- independencia. Actualiza el campo updated_at al modificar registros.
-- =============================================================================
CREATE OR REPLACE FUNCTION catalog.fn_actualizar_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- TRIGGERS DE TABLAS
-- =============================================================================

CREATE OR REPLACE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON catalog.products
    FOR EACH ROW
    WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION catalog.fn_actualizar_updated_at();

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
