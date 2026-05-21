package com.virtualpet.backend.orders.api;

import com.virtualpet.backend.orders.dto.OrderDTO.CreateOrderRequest;
import com.virtualpet.backend.orders.dto.OrderDTO.OrderDetailResponse;
import com.virtualpet.backend.orders.dto.OrderDTO.OrderResponse;
import com.virtualpet.backend.orders.dto.OrderDTO.OrderSummaryResponse;
import com.virtualpet.backend.orders.service.OrderService;
import com.virtualpet.backend.shared.security.UserPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> listOrders(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(orderService.listByUser(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getByIdAndUser(id, currentUser.getId()));
    }
}
