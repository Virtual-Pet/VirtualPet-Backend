-- =============================================================================
-- Flyway Migration: V2.2__assign_riders_to_shipments.sql
-- Descripción: Asigna un repartidor (rider_id) a cada envío en estado ASSIGNED
--              que todavía no tenga uno. Reparte los riders sembrados (V2.0) en
--              round-robin por fecha de creación del envío.
-- Idempotente: solo afecta filas con rider_id IS NULL.
-- =============================================================================

WITH ranked_shipments AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at, id) - 1 AS rn
    FROM shipments.shipments
    WHERE status = 'ASSIGNED' AND rider_id IS NULL
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
