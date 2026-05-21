package com.virtualpet.backend.orders.api;

import com.virtualpet.backend.orders.dto.OrderDTO.CheckoutRequest;
import com.virtualpet.backend.orders.dto.OrderDTO.CheckoutResponse;
import com.virtualpet.backend.orders.service.CheckoutService;
import com.virtualpet.backend.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

  private final CheckoutService checkoutService;

  @PostMapping
  public ResponseEntity<CheckoutResponse> checkout(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @RequestHeader(value = "X-Cart-Session", required = false, defaultValue = "anonymous")
          String cartSession,
      @RequestBody CheckoutRequest request) {
    return ResponseEntity.ok(checkoutService.checkout(currentUser.getId(), cartSession, request));
  }
}
