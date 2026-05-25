package com.virtualpet.orders.dto;

import com.virtualpet.orders.domain.Address;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for /orders read endpoints. Order *creation* now flows through checkout sessions, so the
 * inbound POST shape is gone — see {@link com.virtualpet.orders.dto.CheckoutDTO} for that side.
 */
public final class OrderDTO {

  private OrderDTO() {}

  public record OrderDetailResponse(
      UUID orderId,
      String status,
      BigDecimal total,
      String createdAt,
      Address shippingAddress,
      List<OrderItemDetail> items) {}

  public record OrderItemDetail(
      UUID variantId,
      String sku,
      String productName,
      BigDecimal unitPrice,
      int quantity,
      BigDecimal subtotal) {}

  public record OrderSummaryResponse(
      UUID orderId, String status, BigDecimal total, String createdAt) {}
}
