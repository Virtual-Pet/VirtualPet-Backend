package com.virtualpet.orders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.virtualpet.orders.domain.Address;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.shipments.domain.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Wire DTOs for /orders. Map 1:1 to the OpenAPI schemas. */
public final class OrderDTO {

  private OrderDTO() {}

  public record OrderSummaryDTO(
      UUID orderId,
      OrderStatus status,
      BigDecimal total,
      String currency,
      Instant createdAt,
      UUID shipmentId) {}

  public record OrderLineItemDTO(
      UUID skuId,
      String productName,
      String sku,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}

  public record OrderTotalsDTO(BigDecimal items, BigDecimal shipping, BigDecimal grandTotal) {}

  public record OrderShipmentRefDTO(UUID shipmentId, ShipmentStatus status) {}

  public record OrderResponseDTO(
      UUID orderId,
      UUID customerId,
      OrderStatus status,
      List<OrderLineItemDTO> lineItems,
      OrderTotalsDTO totals,
      String currency,
      Address shippingAddress,
      OrderShipmentRefDTO shipment,
      Instant createdAt,
      boolean requiresInvoice,
      @JsonInclude(JsonInclude.Include.NON_NULL) String billingCuit) {}

  public record InvoiceRequestDTO(
      @NotBlank
      @Size(max = 20)
      @Pattern(regexp = "\\d{2}-\\d{7,8}-\\d", message = "CUIT inválido (formato esperado: XX-XXXXXXXX-X)")
      String cuit) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record RefundSummaryDTO(UUID paymentId, PaymentStatus status) {}

  public record OrderCancellationDTO(
      UUID orderId, OrderStatus status, OrderShipmentRefDTO shipment, RefundSummaryDTO refund) {}

  public record CancelOrderRequestDTO(@Size(max = 200) String reason) {}
}
