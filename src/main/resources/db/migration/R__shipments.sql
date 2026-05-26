-- =============================================================================
-- Virtual Pet MDQ — FUNCIONES, TRIGGERS Y VISTAS (Migraciones Repetibles R__)
-- PostgreSQL 15 - Esquema: shipments
-- =============================================================================

-- =============================================================================
-- FUNCION/TRIGGER: Actualización automática de timestamp (Auditoría)
-- =============================================================================
CREATE OR REPLACE FUNCTION shipments.fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_warehouses_updated_at
    BEFORE UPDATE ON shipments.warehouses
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION shipments.fn_update_timestamp();

CREATE OR REPLACE TRIGGER trg_shipments_updated_at
    BEFORE UPDATE ON shipments.shipments
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION shipments.fn_update_timestamp();

CREATE OR REPLACE TRIGGER trg_assignments_updated_at
    BEFORE UPDATE ON shipments.order_assignments
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION shipments.fn_update_timestamp();

-- =============================================================================
-- VISTA: v_pending_dispatch_shipments (Hoja de ruta para el repartidor)
-- Descripción: Cruza los envíos en tránsito con los datos de contacto
-- y dirección del pedido original.
-- =============================================================================
CREATE OR REPLACE VIEW shipments.v_pending_dispatch_shipments AS
SELECT
    s.id AS shipment_id,
    s.order_id,
    s.courier,
    s.attempts,
    o.contact_name,
    o.contact_lastname,
    o.contact_phone,
    o.shipping_address
FROM shipments.shipments s
         JOIN orders.orders o ON s.order_id = o.id
WHERE s.status = 'IN_TRANSIT';

-- =============================================================================
-- VISTA: v_operator_active_assignments (Dashboard de operario logístico)
-- Descripción: Muestra las asignaciones vigentes de cada operario para
-- facilitar el frontend del Backoffice en el centro de distribución.
-- =============================================================================
CREATE OR REPLACE VIEW shipments.v_operator_active_assignments AS
SELECT
    oa.id AS assignment_id,
    oa.operator_id,
    oa.order_id,
    oa.warehouse_id,
    oa.status AS assignment_status,
    s.status AS shipment_status,
    oa.assigned_at
FROM shipments.order_assignments oa
         JOIN shipments.shipments s ON oa.order_id = s.order_id
WHERE oa.status IN ('ASSIGNED', 'IN_PROGRESS');
