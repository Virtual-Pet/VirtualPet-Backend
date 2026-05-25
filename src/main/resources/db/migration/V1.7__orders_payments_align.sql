-- =============================================================================
-- Align orders.payments + orders.orders with the new checkout-session lifecycle:
--   * payments.gateway → provider, external_reference → provider_payment_id
--   * payments.order_id becomes nullable (payment exists before the order)
--   * payments.session_id FK added (the session a payment is attached to)
--   * payment status set widened to include PROCESSING and PAID/FAILED labels
--   * orders.contact_* fields made nullable (orders now flow from sessions,
--     contact data is denormalized only if/when provided)
--   * orders.session_id FK added with UNIQUE so each session yields one order
-- =============================================================================

ALTER TABLE orders.payments RENAME COLUMN gateway TO provider;
ALTER TABLE orders.payments RENAME COLUMN external_reference TO provider_payment_id;

ALTER TABLE orders.payments ALTER COLUMN order_id DROP NOT NULL;
ALTER TABLE orders.payments ALTER COLUMN idempotency_key DROP NOT NULL;

ALTER TABLE orders.payments ADD COLUMN session_id UUID;
ALTER TABLE orders.payments ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'ARS';
ALTER TABLE orders.payments ADD COLUMN user_id UUID;
ALTER TABLE orders.payments
  ADD CONSTRAINT fk_payment_session
  FOREIGN KEY (session_id) REFERENCES orders.checkout_sessions(id) ON DELETE SET NULL;

ALTER TABLE orders.payments DROP CONSTRAINT chk_gateway;
ALTER TABLE orders.payments
  ADD CONSTRAINT chk_payment_provider CHECK (
    provider IN ('fake','mock','mercadopago','stripe','decidir')
  );

ALTER TABLE orders.payments DROP CONSTRAINT chk_payment_status;
ALTER TABLE orders.payments
  ADD CONSTRAINT chk_payment_status CHECK (
    status IN ('PENDING','PROCESSING','PAID','FAILED','REFUNDED')
  );

CREATE INDEX idx_payments_session_id ON orders.payments (session_id);
CREATE INDEX idx_payments_provider_payment_id ON orders.payments (provider_payment_id);

-- Orders contact fields are now optional; the order body for a confirmed
-- checkout-session does not require duplicating contact info.
ALTER TABLE orders.orders ALTER COLUMN contact_name DROP NOT NULL;
ALTER TABLE orders.orders ALTER COLUMN contact_lastname DROP NOT NULL;
ALTER TABLE orders.orders ALTER COLUMN contact_email DROP NOT NULL;
ALTER TABLE orders.orders ALTER COLUMN contact_phone DROP NOT NULL;

ALTER TABLE orders.orders ADD COLUMN session_id UUID;
ALTER TABLE orders.orders ADD CONSTRAINT uq_orders_session_id UNIQUE (session_id);
ALTER TABLE orders.orders
  ADD CONSTRAINT fk_orders_session
  FOREIGN KEY (session_id) REFERENCES orders.checkout_sessions(id) ON DELETE SET NULL;

-- Order status set widens to include CONFIRMED (set by the confirm orchestrator);
-- the CANCELLED label aligns with the OpenAPI contract while CANCELED stays valid
-- until PR6 backfills + removes the legacy labels.
ALTER TABLE orders.orders DROP CONSTRAINT chk_order_status;
ALTER TABLE orders.orders
  ADD CONSTRAINT chk_order_status CHECK (
    status IN (
      'PENDING_PAYMENT','PAID','CONFIRMED','IN_PREPARATION','PREPARED',
      'SHIPPED','DELIVERED','SHIPPING_FAILED','CANCELED','CANCELLED'
    )
  );

-- Shipment status set widens to include CONFIRMED and IN_TRANSIT (spec-aligned),
-- alongside the legacy values until PR7 backfills + collapses.
ALTER TABLE logistics.shipments DROP CONSTRAINT chk_shipment_status;
ALTER TABLE logistics.shipments
  ADD CONSTRAINT chk_shipment_status CHECK (
    status IN (
      'PENDING','CONFIRMED','IN_PREPARATION','PREPARED','SHIPPED','IN_TRANSIT',
      'DELIVERED','FAILED','CANCELED','CANCELLED'
    )
  );
