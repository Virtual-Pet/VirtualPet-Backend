package com.virtualpet.orders.service;

import com.virtualpet.catalog.service.InventoryService;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderItemEntity;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.domain.PaymentEntity;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.orders.dto.OrderDTO.OrderCancellation;
import com.virtualpet.orders.dto.OrderDTO.OrderShipmentRef;
import com.virtualpet.orders.dto.OrderDTO.RefundSummary;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.repository.PaymentRepository;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.repository.ShipmentRepository;
import com.virtualpet.shipments.service.ShipmentService;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Single point of cancellation. Walks the related entities in one transaction: order → CANCELLED,
 * shipment → CANCELLED, stock restocked, payment refund initiated. Idempotent — a second call on an
 * already-cancelled order returns the same envelope.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelOrchestrator {

  private static final EnumSet<ShipmentStatus> CANCELABLE_SHIPMENT_STATUSES =
      EnumSet.of(ShipmentStatus.CONFIRMED, ShipmentStatus.PREPARED);

  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;
  private final PaymentRepository paymentRepository;
  private final InventoryService inventoryService;
  private final ShipmentService shipmentService;

  @Transactional
  public OrderCancellation cancel(OrderEntity order, String reason) {
    if (order.getStatus() == OrderStatus.CANCELLED) {
      return assembleResponse(order);
    }

    ShipmentEntity shipment =
        shipmentRepository
            .findByOrderId(order.getId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.CONFLICT, "Shipment not found for order " + order.getId()));

    if (!CANCELABLE_SHIPMENT_STATUSES.contains(shipment.getStatus())) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "Cannot cancel order whose shipment is in status " + shipment.getStatus());
    }

    log.info(
        "Cancelling order {} (shipment={}, reason={})", order.getId(), shipment.getId(), reason);

    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);

    ShipmentStatus previousShipmentStatus = shipment.getStatus();
    shipment.setStatus(ShipmentStatus.CANCELLED);
    shipmentRepository.save(shipment);
    shipmentService.recordCancellation(
        shipment.getId(), previousShipmentStatus, reason, order.getUserId());

    Map<UUID, Integer> restockLines = new HashMap<>();
    for (OrderItemEntity item : order.getItems()) {
      restockLines.merge(item.getProductVariantId(), item.getQuantity(), Integer::sum);
    }
    inventoryService.restock(restockLines);

    PaymentEntity refundedPayment = initiateRefund(order.getId());

    return new OrderCancellation(
        order.getId(),
        OrderStatus.CANCELLED,
        new OrderShipmentRef(shipment.getId(), shipment.getStatus()),
        refundedPayment == null
            ? null
            : new RefundSummary(refundedPayment.getId(), refundedPayment.getStatus()));
  }

  private PaymentEntity initiateRefund(UUID orderId) {
    var paymentOpt = paymentRepository.findByOrderId(orderId);
    if (paymentOpt.isEmpty()) {
      log.warn("No payment found to refund for order {}", orderId);
      return null;
    }
    PaymentEntity payment = paymentOpt.get();
    if (payment.getStatus() != PaymentStatus.PAID
        && payment.getStatus() != PaymentStatus.REFUNDED) {
      log.warn(
          "Payment {} for order {} is in status {}; skipping refund",
          payment.getId(),
          orderId,
          payment.getStatus());
      return payment;
    }
    if (payment.getStatus() == PaymentStatus.PAID) {
      payment.setStatus(PaymentStatus.REFUNDED);
      paymentRepository.save(payment);
      log.info("Payment {} marked REFUNDED for order {}", payment.getId(), orderId);
    }
    return payment;
  }

  private OrderCancellation assembleResponse(OrderEntity order) {
    OrderShipmentRef shipmentRef =
        shipmentRepository
            .findByOrderId(order.getId())
            .map(s -> new OrderShipmentRef(s.getId(), s.getStatus()))
            .orElse(null);
    RefundSummary refund =
        paymentRepository
            .findByOrderId(order.getId())
            .map(p -> new RefundSummary(p.getId(), p.getStatus()))
            .orElse(null);
    return new OrderCancellation(order.getId(), order.getStatus(), shipmentRef, refund);
  }
}
