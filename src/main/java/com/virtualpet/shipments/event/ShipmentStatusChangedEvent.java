package com.virtualpet.shipments.event;

import com.virtualpet.shipments.domain.ShipmentStatus;
import java.time.Instant;
import java.util.UUID;

public record ShipmentStatusChangedEvent(
    UUID shipmentId,
    UUID orderId,
    ShipmentStatus newStatus,
    ShipmentStatus previousStatus,
    Instant updatedAt) {}
