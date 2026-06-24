-- Add rider_id to shipments for rider assignment tracking
ALTER TABLE shipments.shipments ADD COLUMN rider_id UUID;

CREATE INDEX idx_shipments_rider_id ON shipments.shipments (rider_id);

-- Drop the old constraint first
ALTER TABLE shipments.shipments DROP CONSTRAINT chk_shipment_status;

-- Migrate legacy IN_TRANSIT shipments to ASSIGNED (rider flow replaces IN_TRANSIT)
UPDATE shipments.shipments SET status = 'ASSIGNED' WHERE status = 'IN_TRANSIT';

-- Add the new constraint
ALTER TABLE shipments.shipments ADD CONSTRAINT chk_shipment_status
        CHECK (status IN ('CONFIRMED', 'PREPARED', 'ASSIGNED', 'DELIVERED', 'RETURNED', 'CANCELLED'));
