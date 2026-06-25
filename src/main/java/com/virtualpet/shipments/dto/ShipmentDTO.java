package com.virtualpet.shipments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.virtualpet.auth.domain.enums.VehicleType;
import com.virtualpet.orders.domain.Address;
import com.virtualpet.shipments.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ShipmentDTO {

  private ShipmentDTO() {}

  /** Rider assigned to the shipment. Null until a rider claims it. */
  public record RiderInfoDTO(String name, String lastname, String phone, VehicleType vehicleType) {}

  public record ShipmentSummaryDTO(
      UUID shipmentId,
      UUID orderId,
      ShipmentStatus status,
      Instant updatedAt,
      String contactName,
      String contactEmail,
      java.math.BigDecimal total,
      Address shippingAddress,
      boolean requiresInvoice,
      String billingCuit,
      @JsonInclude(JsonInclude.Include.NON_NULL) RiderInfoDTO rider,
      short attempts) {}

  public record ShipmentStatusEventDTO(ShipmentStatus status, Instant at) {}

  public record ShipmentResponseDTO(
      UUID shipmentId,
      UUID orderId,
      ShipmentStatus status,
      Address shippingAddress,
      List<ShipmentStatusEventDTO> statusHistory) {}

  public record AdvanceShipmentRequestDTO(@NotNull ShipmentStatus status) {}
}
