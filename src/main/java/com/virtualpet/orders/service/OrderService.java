package com.virtualpet.orders.service;

import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.dto.OrderDTO.OrderDetailResponse;
import com.virtualpet.orders.dto.OrderDTO.OrderItemDetail;
import com.virtualpet.orders.dto.OrderDTO.OrderSummaryResponse;
import com.virtualpet.orders.repository.OrderRepository;
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

  @Transactional(readOnly = true)
  public List<OrderSummaryResponse> listByUser(UUID userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(
            o ->
                new OrderSummaryResponse(
                    o.getId(),
                    o.getStatus().name(),
                    o.getTotal(),
                    o.getCreatedAt() == null ? null : o.getCreatedAt().toString()))
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderDetailResponse getByIdAndUser(UUID orderId, UUID userId) {
    OrderEntity order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

    List<OrderItemDetail> items =
        order.getItems().stream()
            .map(
                i ->
                    new OrderItemDetail(
                        i.getProductVariantId(),
                        i.getSkuSnapshot(),
                        i.getNameSnapshot(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getSubtotal()))
            .toList();

    return new OrderDetailResponse(
        order.getId(),
        order.getStatus().name(),
        order.getTotal(),
        order.getCreatedAt() == null ? null : order.getCreatedAt().toString(),
        order.getShippingAddress(),
        items);
  }
}
