-- Add operator_id to shipments for rider assignment tracking
ALTER TABLE shipments.shipments ADD COLUMN operator_id UUID;

CREATE INDEX idx_shipments_operator_id ON shipments.shipments (operator_id);

-- Migrate legacy IN_TRANSIT shipments to ASSIGNED (rider flow replaces IN_TRANSIT)
UPDATE shipments.shipments SET status = 'ASSIGNED' WHERE status = 'IN_TRANSIT';

-- Replace status constraint: remove IN_TRANSIT/IN_PROGRESS, add ASSIGNED/RETURNED
ALTER TABLE shipments.shipments
    DROP CONSTRAINT chk_shipment_status,
    ADD CONSTRAINT chk_shipment_status
        CHECK (status IN ('CONFIRMED', 'PREPARED', 'ASSIGNED', 'DELIVERED', 'RETURNED', 'CANCELLED'));
