package com.virtualpet.orders.controller;

import com.virtualpet.cart.web.CartSessionCookie;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.orders.dto.CheckoutDTO.GuestCheckoutRequestDTO;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponseDTO;
import com.virtualpet.orders.service.GuestCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoint for guest checkout — no authentication required. */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class GuestCheckoutController {

  private final GuestCheckoutService guestCheckoutService;
  private final VirtualPetProperties properties;

  @PostMapping("/guest")
  public ResponseEntity<OrderConfirmationResponseDTO> guestCheckout(
      @Valid @RequestBody GuestCheckoutRequestDTO request,
      @CookieValue(name = CartSessionCookie.NAME, required = false) String cartSessionId) {

    OrderConfirmationResponseDTO result = guestCheckoutService.checkout(request, cartSessionId);

    ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED);
    if (cartSessionId != null) {
      builder.header(HttpHeaders.SET_COOKIE,
          CartSessionCookie.clear(properties.getCart()).toString());
    }
    return builder.body(result);
  }
}
