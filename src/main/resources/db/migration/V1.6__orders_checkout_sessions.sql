-- =============================================================================
-- Checkout sessions
--   Stores the immutable snapshot of cart line items + totals at the moment the
--   user clicks Checkout, plus the chosen shipping address. The session
--   lifecycle drives /checkout/sessions/{id}/{shipping-address,payment-intents,
--   confirm} until it converges to either CONFIRMED (order created) or
--   FAILED/EXPIRED.
-- =============================================================================

CREATE TABLE orders.checkout_sessions (
       id                  UUID            DEFAULT gen_random_uuid(),
       user_id             UUID            NOT NULL,
       status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
       line_items          JSONB           NOT NULL,
       totals              JSONB           NOT NULL,
       currency            VARCHAR(3)      NOT NULL,
       shipping_address    JSONB           ,
       expires_at          TIMESTAMPTZ     NOT NULL,
       version             BIGINT          NOT NULL DEFAULT 0,
       created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
       updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_checkout_sessions PRIMARY KEY (id),
       CONSTRAINT chk_checkout_session_status CHECK (
         status IN ('PENDING','AWAITING_PAYMENT','PAID','CONFIRMED','EXPIRED','FAILED')
       )
);

CREATE INDEX idx_checkout_sessions_user_status
  ON orders.checkout_sessions (user_id, status);

CREATE INDEX idx_checkout_sessions_expires_at
  ON orders.checkout_sessions (expires_at);
