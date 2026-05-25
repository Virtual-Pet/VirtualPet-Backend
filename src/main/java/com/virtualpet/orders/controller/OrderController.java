package com.virtualpet.orders.controller;

import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.orders.dto.OrderDTO.OrderDetailResponse;
import com.virtualpet.orders.dto.OrderDTO.OrderSummaryResponse;
import com.virtualpet.orders.service.OrderService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-side endpoints for orders. Order creation is owned by checkout sessions ({@code POST
 * /checkout/sessions/{id}/confirm}); cancellation will arrive in a later PR.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public List<OrderSummaryResponse> listOrders(@AuthenticationPrincipal UserPrincipal currentUser) {
    return orderService.listByUser(currentUser.getId());
  }

  @GetMapping("/{id}")
  public OrderDetailResponse getOrder(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID id) {
    return orderService.getByIdAndUser(id, currentUser.getId());
  }
}
