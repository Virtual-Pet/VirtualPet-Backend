-- =============================================================================
-- Virtual Pet MDQ — FUNCIONES, TRIGGERS Y VISTAS (Migraciones Repetibles R__)
-- PostgreSQL 15 - Esquema: orders
-- =============================================================================

-- =============================================================================
-- FUNCION/TRIGGER: Actualización automática de timestamp (Auditoría)
-- =============================================================================
CREATE OR REPLACE FUNCTION orders.fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders.orders
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION orders.fn_update_timestamp();

-- =============================================================================
-- VISTA: v_orders_ready_to_prepare
-- Descripción: Pedidos confirmados (post-checkout) listos para que Logística
-- los prepare, ordenados por antigüedad (FIFO).
-- =============================================================================
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
WHERE o.status = 'CONFIRMED'
ORDER BY o.created_at ASC;
