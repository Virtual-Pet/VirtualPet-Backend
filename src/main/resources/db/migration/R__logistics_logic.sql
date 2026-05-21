-- =============================================================================
-- ACTUALIZACIÓN AUTOMÁTICA DE TIMESTAMP
-- =============================================================================
CREATE OR REPLACE FUNCTION logistics.fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para Warehouses
CREATE TRIGGER trg_warehouses_updated_at
    BEFORE UPDATE ON logistics.warehouses
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION logistics.fn_update_timestamp();

-- Trigger para Shipments
CREATE TRIGGER trg_shipments_updated_at
    BEFORE UPDATE ON logistics.shipments
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION logistics.fn_update_timestamp();

-- Trigger para Order Assignments
CREATE TRIGGER trg_assignments_updated_at
    BEFORE UPDATE ON logistics.order_assignments
    FOR EACH ROW WHEN (OLD.* IS DISTINCT FROM NEW.*)
EXECUTE FUNCTION logistics.fn_update_timestamp();


-- =============================================================================
-- VISTA: v_pending_dispatch_shipments (Hoja de ruta para el repartidor)
-- Descripción: Cruza los envíos listos para entregar con los datos de contacto
-- y dirección del pedido original.
-- =============================================================================
CREATE OR REPLACE VIEW logistics.v_pending_dispatch_shipments AS
SELECT
    s.id AS shipment_id,
    s.order_id,
    s.courier,
    s.attempts,
    o.contact_name,
    o.contact_lastname,
    o.contact_phone,
    o.shipping_address
FROM logistics.shipments s
         JOIN orders.orders o ON s.order_id = o.id
WHERE s.status = 'SHIPPED';

-- =============================================================================
-- VISTA: v_operator_active_assignments (Dashboard de operario logístico)
-- Descripción: Muestra las asignaciones vigentes de cada operario para
-- facilitar el frontend del Backoffice en el centro de distribución.
-- =============================================================================
CREATE OR REPLACE VIEW logistics.v_operator_active_assignments AS
SELECT
    oa.id AS assignment_id,
    oa.operator_id,
    oa.order_id,
    oa.warehouse_id,
    oa.status AS assignment_status,
    s.status AS shipment_status,
    oa.assigned_at
FROM logistics.order_assignments oa
         JOIN logistics.shipments s ON oa.order_id = s.order_id
WHERE oa.status IN ('ASSIGNED', 'IN_PROGRESS');