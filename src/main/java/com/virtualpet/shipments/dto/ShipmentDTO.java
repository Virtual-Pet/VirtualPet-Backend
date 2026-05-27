package com.virtualpet.shipments.dto;

import com.virtualpet.orders.domain.Address;
import com.virtualpet.shipments.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ShipmentDTO {

  private ShipmentDTO() {}

  public record ShipmentSummaryDTO(
      UUID shipmentId, UUID orderId, ShipmentStatus status, Instant updatedAt) {}

  public record ShipmentStatusEventDTO(ShipmentStatus status, Instant at) {}

  public record ShipmentResponseDTO(
      UUID shipmentId,
      UUID orderId,
      ShipmentStatus status,
      Address shippingAddress,
      List<ShipmentStatusEventDTO> statusHistory) {}

  public record AdvanceShipmentRequestDTO(@NotNull ShipmentStatus status) {}
}
