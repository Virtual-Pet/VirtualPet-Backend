
-- =============================================================================
-- VISTAS (Views)
-- =============================================================================

-- Vista de conveniencia para el Backoffice / Módulo de Logística.
-- Muestra rápidamente los pedidos que ya fueron pagados y están listos
-- para ser preparados, ordenados por antigüedad (FIFO).
CREATE OR REPLACE VIEW orders.v_orders_ready_to_prepare AS
SELECT
    o.id,
    o.user_id,
    o.contact_name,
    o.contact_lastname,
    o.total,
    o.shipping_address,
    o.created_at
FROM orders.orders o
WHERE o.status = 'PAID'
ORDER BY o.created_at ASC;


-- =============================================================================
-- ACTUALIZACIÓN AUTOMÁTICA DE TIMESTAMP
-- =============================================================================

CREATE OR REPLACE FUNCTION orders.fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders.orders
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION orders.fn_update_timestamp();