-- =============================================================================
-- SCHEMA: orders
--    Módulo propietario: com.virtualpet.modulo.orders
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS orders;

-- =============================================================================
-- TABLE: orders
-- =============================================================================
CREATE TABLE orders.orders (
       id                  UUID            DEFAULT gen_random_uuid(),
       user_id             UUID            NOT NULL,
       warehouse_id        INT             ,
       contact_name        VARCHAR(50)     NOT NULL,
       contact_lastname    VARCHAR(50)     NOT NULL,
       contact_email       VARCHAR(255)    NOT NULL,
       contact_phone       VARCHAR(30)     NOT NULL,
       status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING_PAYMENT',
       total               NUMERIC(10,2)   NOT NULL,
       shipping_address    JSONB           NOT NULL,
       shipping_attempts   SMALLINT        NOT NULL DEFAULT 0,
       version             BIGINT          NOT NULL DEFAULT 0,
       created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
       updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_orders PRIMARY KEY (id),
       CONSTRAINT chk_order_status CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'IN_PREPARATION', 'PREPARED', 'SHIPPED', 'DELIVERED', 'SHIPPING_FAILED', 'CANCELED')),
       CONSTRAINT chk_order_total CHECK (total >= 0),
       CONSTRAINT chk_max_attempts CHECK (shipping_attempts BETWEEN 0 AND 3)
);

-- =============================================================================
-- TABLE: order_status_history (Auditoría de estados)
-- =============================================================================
CREATE TABLE orders.order_status_history (
         id              UUID            DEFAULT gen_random_uuid(),
         order_id        UUID            NOT NULL,
         prev_status     VARCHAR(50)     NOT NULL,
         new_status      VARCHAR(50)     NOT NULL,
         reason          VARCHAR(100)    ,
         modified_by     UUID            ,
         created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

         CONSTRAINT pk_order_status_history PRIMARY KEY (id),
         CONSTRAINT fk_history_order FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE CASCADE
);

-- =============================================================================
-- TABLE: order_items
-- =============================================================================
CREATE TABLE orders.order_items (
        id                  UUID            DEFAULT gen_random_uuid(),
        order_id            UUID            NOT NULL,
        product_variant_id  UUID            NOT NULL,
        sku_snapshot        VARCHAR(100)    NOT NULL,
        name_snapshot       VARCHAR(255)    NOT NULL,
        unit_price          NUMERIC(10,2)   NOT NULL,
        quantity            INT             NOT NULL,
        subtotal            NUMERIC(10,2)   NOT NULL,

        CONSTRAINT pk_order_items PRIMARY KEY (id),
        CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE CASCADE,
        CONSTRAINT chk_item_price CHECK (unit_price > 0),
        CONSTRAINT chk_item_qty CHECK (quantity > 0),
        CONSTRAINT chk_subtotal_math CHECK (subtotal <= (unit_price * quantity) + 0.01)
);

-- =============================================================================
-- TABLE: payments
-- =============================================================================
CREATE TABLE orders.payments (
         id                  UUID            DEFAULT gen_random_uuid(),
         order_id            UUID            NOT NULL,
         gateway             VARCHAR(50)     NOT NULL,
         external_reference  VARCHAR(255)    ,
         amount              NUMERIC(10,2)   NOT NULL,
         status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
         idempotency_key     VARCHAR(255)    NOT NULL,
         created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

         CONSTRAINT pk_payments PRIMARY KEY (id),
         CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE RESTRICT,
         CONSTRAINT chk_gateway CHECK (gateway IN ('MOCK', 'MERCADOPAGO', 'STRIPE', 'DECIDIR')),
         CONSTRAINT chk_payment_amount CHECK (amount > 0),
         CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'REFUNDED')),
         CONSTRAINT uq_payment_idempotency UNIQUE (idempotency_key)
);

-- =============================================================================
-- TABLE: payment_status_history (Auditoría de pagos)
-- =============================================================================
CREATE TABLE orders.payment_status_history (
       id                  UUID        DEFAULT gen_random_uuid(),
       payment_id          UUID        NOT NULL,
       prev_status         VARCHAR(50) NOT NULL,
       new_status          VARCHAR(50) NOT NULL,
       created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_payment_status_history PRIMARY KEY (id),
       CONSTRAINT fk_history_payment FOREIGN KEY (payment_id) REFERENCES orders.payments(id) ON DELETE CASCADE
);

-- =============================================================================
-- ÍNDICES
-- =============================================================================
CREATE INDEX idx_orders_user_id ON orders.orders (user_id);
CREATE INDEX idx_orders_status ON orders.orders (status);
CREATE INDEX idx_orders_created_at ON orders.orders (created_at DESC);
CREATE INDEX idx_items_order_id ON orders.order_items (order_id);
CREATE INDEX idx_payments_order_id ON orders.payments (order_id);

CREATE INDEX idx_order_history_id ON orders.order_status_history (order_id);
CREATE INDEX idx_payment_history_id ON orders.payment_status_history (payment_id);