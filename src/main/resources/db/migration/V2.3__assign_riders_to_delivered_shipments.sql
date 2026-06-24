-- =============================================================================
-- Flyway Migration: V2.3__assign_riders_to_delivered_shipments.sql
-- Descripción: Asigna un repartidor (rider_id) a los envíos ya entregados o
--              devueltos (DELIVERED / RETURNED) que todavía no tengan uno.
--              Estos estados implican que un rider intervino en la entrega, por
--              lo que el rider_id NULL es una inconsistencia respecto del flujo.
--              Los estados previos a la asignación (CONFIRMED, PREPARED,
--              CANCELLED) se dejan deliberadamente con rider_id NULL.
--              Reparte los riders sembrados (V2.0) en round-robin por fecha de
--              creación del envío, mismo criterio que V2.2.
-- Integridad: el rider_id sólo puede tomar ids reales de auth.users con
--             ROLE_RIDER (no existe FK física por ser referencia cross-schema).
-- Idempotente: sólo afecta filas con rider_id IS NULL.
-- =============================================================================

WITH ranked_shipments AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at, id) - 1 AS rn
    FROM shipments.shipments
    WHERE status IN ('DELIVERED', 'RETURNED') AND rider_id IS NULL
),
riders AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY email) - 1 AS idx
    FROM auth.users
    WHERE role = 'ROLE_RIDER'
),
rider_count AS (
    SELECT COUNT(*) AS n FROM riders
)
UPDATE shipments.shipments s
SET rider_id = r.id
FROM ranked_shipments rs
JOIN rider_count rc ON rc.n > 0
JOIN riders r ON r.idx = rs.rn % rc.n
WHERE s.id = rs.id;
