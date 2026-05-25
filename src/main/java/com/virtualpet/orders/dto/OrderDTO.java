package com.virtualpet.orders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.virtualpet.orders.domain.Address;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.shipments.domain.ShipmentStatus;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Wire DTOs for /orders. Map 1:1 to the OpenAPI schemas. */
public final class OrderDTO {

  private OrderDTO() {}

  public record OrderSummary(
      UUID orderId,
      OrderStatus status,
      BigDecimal total,
      String currency,
      Instant createdAt,
      UUID shipmentId) {}

  public record OrderLineItem(
      UUID skuId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}

  public record OrderTotals(BigDecimal items, BigDecimal shipping, BigDecimal grandTotal) {}

  public record OrderShipmentRef(UUID shipmentId, ShipmentStatus status) {}

  public record OrderResponse(
      UUID orderId,
      UUID customerId,
      OrderStatus status,
      List<OrderLineItem> lineItems,
      OrderTotals totals,
      String currency,
      Address shippingAddress,
      OrderShipmentRef shipment,
      Instant createdAt) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record RefundSummary(UUID paymentId, PaymentStatus status) {}

  public record OrderCancellation(
      UUID orderId, OrderStatus status, OrderShipmentRef shipment, RefundSummary refund) {}

  public record CancelOrderRequest(@Size(max = 200) String reason) {}
}
