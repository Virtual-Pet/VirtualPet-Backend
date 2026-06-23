package com.virtualpet.orders.controller;

import com.virtualpet.common.pagination.CursorPage;
import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.dto.OrderDTO.CancelOrderRequestDTO;
import com.virtualpet.orders.dto.OrderDTO.InvoiceRequestDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderCancellationDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderResponseDTO;
import com.virtualpet.orders.dto.OrderDTO.OrderSummaryDTO;
import com.virtualpet.orders.service.OrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read + cancel endpoints for orders. Authentication is required; CUSTOMER sees only their own
 * orders, EMPLOYEE/ADMIN see everything and may filter by {@code user}.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public CursorPage<OrderSummaryDTO> list(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false, name = "user") UUID userFilter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    return orderService.list(
        currentUser.getId(), isCustomer(currentUser), status, userFilter, cursor, limit);
  }

  @GetMapping("/{id}")
  public OrderResponseDTO get(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID id) {
    return orderService.getById(id, currentUser.getId(), isCustomer(currentUser));
  }

  @GetMapping("/{id}/track")
  public OrderResponseDTO track(@PathVariable UUID id, @RequestParam String token) {
    return orderService.getByTrackingToken(id, token);
  }

  @PostMapping("/{id}/cancel")
  public OrderCancellationDTO cancel(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID id,
      @Valid @RequestBody(required = false) CancelOrderRequestDTO request) {
    String reason = request == null ? null : request.reason();
    return orderService.cancel(id, currentUser.getId(), isCustomer(currentUser), reason);
  }

  @PostMapping("/{id}/invoice")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void requestInvoice(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID id,
      @Valid @RequestBody InvoiceRequestDTO request) {
    orderService.requestInvoice(id, request.cuit(), currentUser.getId());
  }

  private static boolean isCustomer(UserPrincipal user) {
    String role = user.getRole();
    return role != null && role.endsWith("CUSTOMER");
  }
}
