package com.virtualpet.shipments.dto;

import com.virtualpet.orders.domain.Address;
import com.virtualpet.shipments.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ShipmentDTO {

  private ShipmentDTO() {}

  public record ShipmentSummary(
      UUID shipmentId, UUID orderId, ShipmentStatus status, Instant updatedAt) {}

  public record ShipmentStatusEvent(ShipmentStatus status, Instant at) {}

  public record ShipmentResponse(
      UUID shipmentId,
      UUID orderId,
      ShipmentStatus status,
      Address shippingAddress,
      List<ShipmentStatusEvent> statusHistory) {}

  public record AdvanceShipmentRequest(@NotNull ShipmentStatus status) {}
}
