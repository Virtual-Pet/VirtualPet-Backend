package com.virtualpet.orders.controller;

import com.virtualpet.orders.dto.CheckoutDTO.GuestCheckoutRequestDTO;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponseDTO;
import com.virtualpet.orders.service.GuestCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoint for guest checkout — no authentication required. */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class GuestCheckoutController {

  private final GuestCheckoutService guestCheckoutService;

  @PostMapping("/guest")
  @ResponseStatus(HttpStatus.CREATED)
  public OrderConfirmationResponseDTO guestCheckout(
      @Valid @RequestBody GuestCheckoutRequestDTO request) {
    return guestCheckoutService.checkout(request);
  }
}
