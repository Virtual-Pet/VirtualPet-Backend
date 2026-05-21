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
            List<OrderItemRequest> items
    ) {}

    public record OrderItemRequest(
            UUID productVariantId,
            String sku,
            String name,
            BigDecimal unitPrice,
            Integer quantity
    ) {}

    public record OrderResponse(
            UUID orderId,
            String status,
            BigDecimal total
    ) {}
}
