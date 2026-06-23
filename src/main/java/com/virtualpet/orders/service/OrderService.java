package com.virtualpet.orders.service;

import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.pagination.Cursor;
import com.virtualpet.common.pagination.CursorCodec;
import com.virtualpet.common.pagination.CursorPage;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.dto.OrderDTO.OrderCancellationDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderLineItemDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderResponseDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderShipmentRefDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderSummaryDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderTotalsDTO;
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
  public CursorPage<OrderSummaryDTO> list(
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

    List<OrderSummaryDTO> data = rows.stream().map(this::toSummary).toList();

    String nextCursor =
        hasMore
            ? cursorCodec.encode(Cursor.of(rows.getLast().getCreatedAt(), rows.getLast().getId()))
            : null;
    return CursorPage.of(data, effectiveLimit, nextCursor);
  }

  /* ---------- Detail ---------- */

  @Transactional(readOnly = true)
  public OrderResponseDTO getById(UUID orderId, UUID callerId, boolean isCustomer) {
    OrderEntity order = loadAccessible(orderId, callerId, isCustomer);
    return toResponse(order);
  }

  /* ---------- Guest tracking ---------- */

  @Transactional(readOnly = true)
  public OrderResponseDTO getByTrackingToken(UUID orderId, String token) {
    OrderEntity order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    if (token == null || !token.equals(order.getTrackingToken())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Invalid tracking token");
    }
    return toResponse(order);
  }

  /* ---------- Invoice ---------- */

  @Transactional
  public void requestInvoice(UUID orderId, String cuit, UUID callerId) {
    OrderEntity order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    if (!order.getUserId().equals(callerId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Order not found");
    }
    if (order.getStatus() == OrderStatus.CANCELLED) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_CONTENT, "Cannot request invoice for a cancelled order");
    }
    order.setRequiresInvoice(true);
    order.setBillingCuit(cuit);
    orderRepository.save(order);
  }

  /* ---------- Chatbot helpers ---------- */

  @Transactional(readOnly = true)
  public List<OrderSummaryDTO> getActiveOrdersForChatbot(UUID userId) {
    Specification<OrderEntity> spec =
        Specification.allOf(
            OrderSpecifications.byUser(userId),
            (root, query, cb) -> cb.notEqual(root.get("status"), OrderStatus.CANCELLED));
    Sort sort = Sort.by(Sort.Order.desc("createdAt"));
    return orderRepository.findAll(spec, PageRequest.of(0, 10, sort)).getContent().stream()
        .map(this::toSummary)
        .toList();
  }

  /* ---------- Cancel ---------- */

  @Transactional
  public OrderCancellationDTO cancel(
      UUID orderId, UUID callerId, boolean isCustomer, String reason) {
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

  private OrderSummaryDTO toSummary(OrderEntity order) {
    UUID shipmentId =
        shipmentRepository.findByOrderId(order.getId()).map(ShipmentEntity::getId).orElse(null);
    return new OrderSummaryDTO(
        order.getId(),
        order.getStatus(),
        order.getTotal(),
        CURRENCY,
        order.getCreatedAt(),
        shipmentId);
  }

  private OrderResponseDTO toResponse(OrderEntity order) {
    BigDecimal itemsTotal =
        order.getItems().stream()
            .map(i -> i.getSubtotal() == null ? BigDecimal.ZERO : i.getSubtotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal shipping = order.getTotal().subtract(itemsTotal).max(BigDecimal.ZERO);

    List<OrderLineItemDTO> lineItems =
        order.getItems().stream()
            .map(
                i ->
                    new OrderLineItemDTO(
                        i.getProductVariantId(),
                        i.getNameSnapshot(),
                        i.getSkuSnapshot(),
                        i.getQuantity() == null ? 0 : i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()))
            .toList();

    OrderShipmentRefDTO shipmentRef =
        shipmentRepository
            .findByOrderId(order.getId())
            .map(s -> new OrderShipmentRefDTO(s.getId(), s.getStatus()))
            .orElse(null);

    return new OrderResponseDTO(
        order.getId(),
        order.getUserId(),
        order.getStatus(),
        lineItems,
        new OrderTotalsDTO(itemsTotal, shipping, order.getTotal()),
        CURRENCY,
        order.getShippingAddress(),
        shipmentRef,
        order.getCreatedAt(),
        order.isRequiresInvoice(),
        order.getBillingCuit());
  }
}
