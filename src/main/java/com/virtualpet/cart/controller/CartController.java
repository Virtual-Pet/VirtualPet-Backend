package com.virtualpet.cart.controller;

import com.virtualpet.cart.dto.CartDTO.CartItemQuantityDTO;
import com.virtualpet.cart.dto.CartDTO.CartViewDTO;
import com.virtualpet.cart.dto.CartDTO.UpdateQuantityRequestDTO;
import com.virtualpet.cart.service.CartService;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart API. Authenticated users use /cart (keyed by userId). Anonymous users use
 * /cart/session/{sessionId} (keyed by a client-generated UUID stored in a browser cookie).
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  /* ---------- Authenticated cart ---------- */

  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public CartViewDTO getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
    return cartService.getCart(currentUser.getId());
  }

  @PutMapping("/items/{skuId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public CartItemQuantityDTO putItem(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID skuId,
      @Valid @RequestBody UpdateQuantityRequestDTO request) {
    return cartService.putItem(currentUser.getId(), skuId, request.quantity());
  }

  @DeleteMapping("/items/{skuId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeItem(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID skuId) {
    cartService.removeItem(currentUser.getId(), skuId);
  }

  /* ---------- Anonymous cart ---------- */

  @GetMapping("/session/{sessionId}")
  public CartViewDTO getAnonCart(@PathVariable @NotBlank String sessionId) {
    return cartService.getAnonCart(sessionId);
  }

  @PutMapping("/session/{sessionId}/items/{skuId}")
  public CartItemQuantityDTO putAnonItem(
      @PathVariable @NotBlank String sessionId,
      @PathVariable UUID skuId,
      @Valid @RequestBody UpdateQuantityRequestDTO request) {
    return cartService.putAnonItem(sessionId, skuId, request.quantity());
  }

  @DeleteMapping("/session/{sessionId}/items/{skuId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeAnonItem(
      @PathVariable @NotBlank String sessionId, @PathVariable UUID skuId) {
    cartService.removeAnonItem(sessionId, skuId);
  }
}
