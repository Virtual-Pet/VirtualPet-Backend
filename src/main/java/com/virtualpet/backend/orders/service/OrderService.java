package com.virtualpet.backend.orders.service;

import com.virtualpet.backend.orders.domain.*;
import com.virtualpet.backend.orders.dto.OrderDTO.*;
import com.virtualpet.backend.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(UUID currentUserId, CreateOrderRequest request) {

        // Instanciamos la orden base
        OrderEntity order = OrderEntity.builder()
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

            OrderItemEntity item = OrderItemEntity.builder()
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

        return new OrderResponse(savedOrder.getId(), savedOrder.getStatus().name(), savedOrder.getTotal());
    }
}
