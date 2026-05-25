-- =============================================================================
-- Collapse orders.status to the OpenAPI contract ({CONFIRMED, CANCELLED}).
-- Legacy labels are backfilled:
--   PAID, IN_PREPARATION, PREPARED, SHIPPED, DELIVERED → CONFIRMED
--   CANCELED, PENDING_PAYMENT, SHIPPING_FAILED       → CANCELLED
-- The spelling 'CANCELED' is normalized to 'CANCELLED' so the CHECK can be
-- tightened to the two-value set.
-- =============================================================================

UPDATE orders.orders
   SET status = 'CONFIRMED'
 WHERE status IN ('PAID','IN_PREPARATION','PREPARED','SHIPPED','DELIVERED');

UPDATE orders.orders
   SET status = 'CANCELLED'
 WHERE status IN ('CANCELED','PENDING_PAYMENT','SHIPPING_FAILED');

ALTER TABLE orders.orders DROP CONSTRAINT chk_order_status;
ALTER TABLE orders.orders
  ADD CONSTRAINT chk_order_status CHECK (status IN ('CONFIRMED','CANCELLED'));
