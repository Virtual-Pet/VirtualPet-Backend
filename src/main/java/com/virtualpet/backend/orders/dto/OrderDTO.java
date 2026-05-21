package com.virtualpet.backend.orders.dto;

import com.virtualpet.backend.orders.domain.ShippingAddress;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class OrderDTO {

  public record CreateOrderRequest(
      String contactName,
      String contactLastname,
      String contactEmail,
      String contactPhone,
      ShippingAddress shippingAddress,
      List<OrderItemRequest> items) {}

  public record OrderItemRequest(
      UUID productVariantId, String sku, String name, BigDecimal unitPrice, Integer quantity) {}

  public record OrderResponse(UUID orderId, String status, BigDecimal total) {}

  public record CheckoutRequest(
      String contactName,
      String contactLastname,
      String contactEmail,
      String contactPhone,
      ShippingAddress shippingAddress) {}

  public record CheckoutResponse(
      String orderId, String status, java.math.BigDecimal total, String paymentUrl) {}

  public record OrderDetailResponse(
      String orderId,
      String status,
      java.math.BigDecimal total,
      String createdAt,
      ShippingAddress shippingAddress,
      String contactName,
      String contactLastname,
      String contactEmail,
      String contactPhone,
      java.util.List<OrderItemDetail> items) {}

  public record OrderItemDetail(
      String variantId,
      String sku,
      String productName,
      java.math.BigDecimal unitPrice,
      int quantity,
      java.math.BigDecimal subtotal) {}

  public record OrderSummaryResponse(
      String orderId,
      String status,
      java.math.BigDecimal total,
      String createdAt,
      String contactName,
      String contactEmail) {}
}
