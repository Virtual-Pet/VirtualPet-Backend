package com.virtualpet.orders.service;

import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderItemEntity;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.dto.OrderDTO.CreateOrderRequest;
import com.virtualpet.orders.dto.OrderDTO.OrderDetailResponse;
import com.virtualpet.orders.dto.OrderDTO.OrderItemDetail;
import com.virtualpet.orders.dto.OrderDTO.OrderItemRequest;
import com.virtualpet.orders.dto.OrderDTO.OrderResponse;
import com.virtualpet.orders.dto.OrderDTO.OrderSummaryResponse;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;

  @Transactional
  public OrderResponse createOrder(UUID currentUserId, CreateOrderRequest request) {

    // Instanciamos la orden base
    OrderEntity order =
        OrderEntity.builder()
            .userId(currentUserId)
            .contactName(request.contactName())
            .contactLastname(request.contactLastname())
            .contactEmail(request.contactEmail())
            .contactPhone(request.contactPhone())
            .shippingAddress(request.shippingAddress())
            .status(OrderStatus.PENDING_PAYMENT)
            .shippingAttempts((short) 0)
            .build();

    // Procesamos los items y calculamos el total internamente
    BigDecimal grandTotal = BigDecimal.ZERO;

    for (OrderItemRequest itemReq : request.items()) {
      BigDecimal subtotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
      grandTotal = grandTotal.add(subtotal);

      OrderItemEntity item =
          OrderItemEntity.builder()
              .productVariantId(itemReq.productVariantId())
              .skuSnapshot(itemReq.sku())
              .nameSnapshot(itemReq.name())
              .unitPrice(itemReq.unitPrice())
              .quantity(itemReq.quantity())
              .subtotal(subtotal)
              .build();

      order.addItem(item); // Esto asocia el item a la orden automáticamente
    }

    order.setTotal(grandTotal);

    OrderEntity savedOrder = orderRepository.save(order);

    return new OrderResponse(
        savedOrder.getId(), savedOrder.getStatus().name(), savedOrder.getTotal());
  }

  @Transactional(readOnly = true)
  public List<OrderSummaryResponse> listByUser(UUID userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(
            o ->
                new OrderSummaryResponse(
                    o.getId().toString(),
                    o.getStatus().name(),
                    o.getTotal(),
                    o.getCreatedAt().toString(),
                    o.getContactName() + " " + o.getContactLastname(),
                    o.getContactEmail()))
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderDetailResponse getByIdAndUser(UUID orderId, UUID userId) {
    OrderEntity order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

    List<OrderItemDetail> items =
        order.getItems().stream()
            .map(
                i ->
                    new OrderItemDetail(
                        i.getProductVariantId().toString(),
                        i.getSkuSnapshot(),
                        i.getNameSnapshot(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getSubtotal()))
            .toList();

    return new OrderDetailResponse(
        order.getId().toString(),
        order.getStatus().name(),
        order.getTotal(),
        order.getCreatedAt().toString(),
        order.getShippingAddress(),
        order.getContactName(),
        order.getContactLastname(),
        order.getContactEmail(),
        order.getContactPhone(),
        items);
  }
}
