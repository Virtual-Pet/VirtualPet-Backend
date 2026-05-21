package com.virtualpet.backend.orders.api;

import com.virtualpet.backend.shared.security.UserPrincipal;
import com.virtualpet.backend.orders.dto.OrderDTO.CreateOrderRequest;
import com.virtualpet.backend.orders.dto.OrderDTO.OrderResponse;
import com.virtualpet.backend.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
