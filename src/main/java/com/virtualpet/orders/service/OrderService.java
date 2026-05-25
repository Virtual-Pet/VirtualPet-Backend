package com.virtualpet.orders.service;

import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.pagination.Cursor;
import com.virtualpet.common.pagination.CursorCodec;
import com.virtualpet.common.pagination.CursorPage;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.dto.OrderDTO.OrderCancellation;
import com.virtualpet.orders.dto.OrderDTO.OrderLineItem;
import com.virtualpet.orders.dto.OrderDTO.OrderResponse;
import com.virtualpet.orders.dto.OrderDTO.OrderShipmentRef;
import com.virtualpet.orders.dto.OrderDTO.OrderSummary;
import com.virtualpet.orders.dto.OrderDTO.OrderTotals;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.spec.OrderSpecifications;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.repository.ShipmentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private static final String CURRENCY = "ARS";

  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;
  private final CancelOrchestrator cancelOrchestrator;
  private final CursorCodec cursorCodec;

  /* ---------- List ---------- */

  /**
   * Lists orders with cursor pagination. A CUSTOMER caller is restricted to their own orders;
   * EMPLOYEE/ADMIN see every order and may filter by {@code userFilter}.
   */
  @Transactional(readOnly = true)
  public CursorPage<OrderSummary> list(
      UUID callerId,
      boolean isCustomer,
      OrderStatus status,
      UUID userFilter,
      String cursor,
      int limit) {
    int effectiveLimit = clampLimit(limit);
    Cursor decoded = cursorCodec.decode(cursor);

    UUID effectiveUserFilter = isCustomer ? callerId : userFilter;

    Specification<OrderEntity> spec =
        Specification.allOf(
            Stream.of(
                    OrderSpecifications.byUser(effectiveUserFilter),
                    OrderSpecifications.byStatus(status),
                    OrderSpecifications.afterCursor(decoded))
                .filter(Objects::nonNull)
                .toList());

    Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    List<OrderEntity> rows =
        orderRepository.findAll(spec, PageRequest.of(0, effectiveLimit + 1, sort)).getContent();

    boolean hasMore = rows.size() > effectiveLimit;
    if (hasMore) {
      rows = rows.subList(0, effectiveLimit);
    }

    List<OrderSummary> data = rows.stream().map(this::toSummary).toList();

    String nextCursor =
        hasMore
            ? cursorCodec.encode(Cursor.of(rows.getLast().getCreatedAt(), rows.getLast().getId()))
            : null;
    return CursorPage.of(data, effectiveLimit, nextCursor);
  }

  /* ---------- Detail ---------- */

  @Transactional(readOnly = true)
  public OrderResponse getById(UUID orderId, UUID callerId, boolean isCustomer) {
    OrderEntity order = loadAccessible(orderId, callerId, isCustomer);
    return toResponse(order);
  }

  /* ---------- Cancel ---------- */

  @Transactional
  public OrderCancellation cancel(UUID orderId, UUID callerId, boolean isCustomer, String reason) {
    OrderEntity order = loadAccessible(orderId, callerId, isCustomer);
    return cancelOrchestrator.cancel(order, reason);
  }

  /* ---------- Internals ---------- */

  private OrderEntity loadAccessible(UUID orderId, UUID callerId, boolean isCustomer) {
    OrderEntity order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    if (isCustomer && !order.getUserId().equals(callerId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Order not found");
    }
    return order;
  }

  private int clampLimit(int requested) {
    if (requested <= 0) {
      return 20;
    }
    return Math.min(requested, 100);
  }

  private OrderSummary toSummary(OrderEntity order) {
    UUID shipmentId =
        shipmentRepository.findByOrderId(order.getId()).map(ShipmentEntity::getId).orElse(null);
    return new OrderSummary(
        order.getId(),
        order.getStatus(),
        order.getTotal(),
        CURRENCY,
        order.getCreatedAt(),
        shipmentId);
  }

  private OrderResponse toResponse(OrderEntity order) {
    BigDecimal itemsTotal =
        order.getItems().stream()
            .map(i -> i.getSubtotal() == null ? BigDecimal.ZERO : i.getSubtotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal shipping = order.getTotal().subtract(itemsTotal).max(BigDecimal.ZERO);

    List<OrderLineItem> lineItems =
        order.getItems().stream()
            .map(
                i ->
                    new OrderLineItem(
                        i.getProductVariantId(),
                        i.getQuantity() == null ? 0 : i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()))
            .toList();

    OrderShipmentRef shipmentRef =
        shipmentRepository
            .findByOrderId(order.getId())
            .map(s -> new OrderShipmentRef(s.getId(), s.getStatus()))
            .orElse(null);

    return new OrderResponse(
        order.getId(),
        order.getUserId(),
        order.getStatus(),
        lineItems,
        new OrderTotals(itemsTotal, shipping, order.getTotal()),
        CURRENCY,
        order.getShippingAddress(),
        shipmentRef,
        order.getCreatedAt());
  }
}
