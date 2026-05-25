package com.virtualpet.orders.service;

import com.virtualpet.cart.service.CartService;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.catalog.service.InventoryService;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.CheckoutSessionEntity;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderItemEntity;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.domain.PaymentEntity;
import com.virtualpet.orders.domain.SessionLineItem;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponse;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.repository.PaymentRepository;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.repository.ShipmentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent confirmation step. Both the user-driven /confirm endpoint and the webhook converge
 * here; whichever path runs first creates the order + shipment, decrements stock, and clears the
 * cart. The other path observes the existing order and exits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmOrchestrator {

  private static final int DEFAULT_WAREHOUSE_ID = 1;

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final ShipmentRepository shipmentRepository;
  private final ProductVariantRepository variantRepository;
  private final InventoryService inventoryService;
  private final CheckoutSessionService sessionService;
  private final CartService cartService;

  @Transactional
  public OrderConfirmationResponse confirmPaidSession(
      CheckoutSessionEntity session, PaymentEntity payment) {
    var existing = orderRepository.findBySessionId(session.getId());
    if (existing.isPresent()) {
      OrderEntity order = existing.get();
      UUID shipmentId =
          shipmentRepository.findByOrderId(order.getId()).map(ShipmentEntity::getId).orElse(null);
      return new OrderConfirmationResponse(order.getId(), shipmentId, order.getStatus().name());
    }

    if (session.getShippingAddress() == null) {
      throw new ApiException(
          HttpStatus.CONFLICT, "Cannot confirm a session without a shipping address");
    }

    Map<UUID, ProductVariantEntity> variants = fetchVariants(session.getLineItems());

    OrderEntity order =
        OrderEntity.builder()
            .userId(session.getUserId())
            .sessionId(session.getId())
            .status(OrderStatus.CONFIRMED)
            .total(session.getTotals().grandTotal())
            .shippingAddress(session.getShippingAddress())
            .warehouseId(DEFAULT_WAREHOUSE_ID)
            .build();
    for (SessionLineItem line : session.getLineItems()) {
      ProductVariantEntity variant = variants.get(line.skuId());
      OrderItemEntity item =
          OrderItemEntity.builder()
              .productVariantId(line.skuId())
              .skuSnapshot(variant.getSku())
              .nameSnapshot(variant.getProduct().getName())
              .unitPrice(line.unitPrice())
              .quantity(line.quantity())
              .subtotal(line.subtotal())
              .build();
      order.addItem(item);
    }
    OrderEntity savedOrder = orderRepository.saveAndFlush(order);

    Map<UUID, Integer> stockLines = new HashMap<>();
    for (SessionLineItem line : session.getLineItems()) {
      stockLines.put(line.skuId(), line.quantity());
    }
    inventoryService.decrementStock(stockLines);

    ShipmentEntity shipment =
        ShipmentEntity.builder()
            .orderId(savedOrder.getId())
            .warehouseId(DEFAULT_WAREHOUSE_ID)
            .courier("OWN_DELIVERY")
            .status(ShipmentStatus.CONFIRMED)
            .build();
    ShipmentEntity savedShipment = shipmentRepository.save(shipment);

    payment.setOrderId(savedOrder.getId());
    paymentRepository.save(payment);

    session.setStatus(SessionStatus.CONFIRMED);
    sessionService.save(session);

    cartService.clearCart(session.getUserId());

    log.info(
        "Order confirmed: orderId={}, shipmentId={}, sessionId={}",
        savedOrder.getId(),
        savedShipment.getId(),
        session.getId());
    return new OrderConfirmationResponse(
        savedOrder.getId(), savedShipment.getId(), savedOrder.getStatus().name());
  }

  private Map<UUID, ProductVariantEntity> fetchVariants(List<SessionLineItem> lines) {
    List<UUID> ids = lines.stream().map(SessionLineItem::skuId).toList();
    Map<UUID, ProductVariantEntity> byId = new HashMap<>();
    for (UUID id : ids) {
      ProductVariantEntity v =
          variantRepository
              .findByIdWithProduct(id)
              .orElseThrow(
                  () -> new ApiException(HttpStatus.CONFLICT, "SKU no longer available: " + id));
      byId.put(id, v);
    }
    return byId;
  }
}
