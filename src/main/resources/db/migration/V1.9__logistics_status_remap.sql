-- =============================================================================
-- Collapse logistics.shipments.status to the OpenAPI contract:
--   {CONFIRMED, PREPARED, IN_TRANSIT, DELIVERED, CANCELLED}.
-- Legacy labels backfill:
--   PENDING, IN_PREPARATION  → CONFIRMED
--   SHIPPED                  → IN_TRANSIT
--   FAILED, CANCELED         → CANCELLED
-- =============================================================================

UPDATE logistics.shipments
   SET status = 'CONFIRMED'
 WHERE status IN ('PENDING','IN_PREPARATION');

UPDATE logistics.shipments
   SET status = 'IN_TRANSIT'
 WHERE status = 'SHIPPED';

UPDATE logistics.shipments
   SET status = 'CANCELLED'
 WHERE status IN ('FAILED','CANCELED');

ALTER TABLE logistics.shipments DROP CONSTRAINT chk_shipment_status;
ALTER TABLE logistics.shipments
  ADD CONSTRAINT chk_shipment_status CHECK (
    status IN ('CONFIRMED','PREPARED','IN_TRANSIT','DELIVERED','CANCELLED')
  );

-- The audit table widens to accept the new labels (it also stored prev/new for
-- transitions, so legacy values may still appear in historical rows — keep
-- them allowed for read-back).
ALTER TABLE logistics.shipment_status
  ALTER COLUMN prev_status TYPE VARCHAR(50);
ALTER TABLE logistics.shipment_status
  ALTER COLUMN new_status TYPE VARCHAR(50);
