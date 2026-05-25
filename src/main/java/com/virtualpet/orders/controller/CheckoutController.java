package com.virtualpet.orders.controller;

import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.orders.dto.CheckoutDTO.CheckoutSessionResponse;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponse;
import com.virtualpet.orders.dto.CheckoutDTO.PaymentIntentResponse;
import com.virtualpet.orders.dto.CheckoutDTO.SetShippingAddressRequest;
import com.virtualpet.orders.service.CheckoutSessionService;
import com.virtualpet.orders.service.CheckoutSessionService.StartResult;
import com.virtualpet.orders.service.PaymentService;
import com.virtualpet.orders.service.PaymentService.ConfirmOutcome;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Checkout sessions + payment intent + confirmation endpoints. All require CUSTOMER role. */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

  private final CheckoutSessionService sessionService;
  private final PaymentService paymentService;

  @PostMapping("/cart/checkout")
  public ResponseEntity<CheckoutSessionResponse> startCheckout(
      @AuthenticationPrincipal UserPrincipal currentUser) {
    StartResult result = sessionService.startCheckout(currentUser.getId());
    HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
    return ResponseEntity.status(status).body(result.response());
  }

  @GetMapping("/checkout/sessions/{id}")
  public CheckoutSessionResponse getSession(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID id) {
    return sessionService.getSession(id, currentUser.getId());
  }

  @PutMapping("/checkout/sessions/{id}/shipping-address")
  public CheckoutSessionResponse setShippingAddress(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID id,
      @Valid @RequestBody SetShippingAddressRequest request) {
    return sessionService.setShippingAddress(id, currentUser.getId(), request);
  }

  @PostMapping("/checkout/sessions/{id}/payment-intents")
  @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
  public PaymentIntentResponse createIntent(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {
    return paymentService.createIntent(id, currentUser.getId(), idempotencyKey);
  }

  @PostMapping("/checkout/sessions/{id}/confirm")
  public ResponseEntity<OrderConfirmationResponse> confirm(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {
    ConfirmOutcome outcome = paymentService.confirm(id, currentUser.getId(), idempotencyKey);
    return ResponseEntity.status(outcome.status()).body(outcome.body());
  }
}
