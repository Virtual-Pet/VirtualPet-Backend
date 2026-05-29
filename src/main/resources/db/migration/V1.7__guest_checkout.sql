-- Guest checkout: make user_id nullable and add tracking token
ALTER TABLE orders.orders ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE orders.orders ADD COLUMN tracking_token VARCHAR(64);
CREATE UNIQUE INDEX idx_orders_tracking_token ON orders.orders (tracking_token)
    WHERE tracking_token IS NOT NULL;
