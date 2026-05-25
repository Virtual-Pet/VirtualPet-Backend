package com.virtualpet.cart.controller;

import com.virtualpet.cart.dto.CartDTO.Cart;
import com.virtualpet.cart.dto.CartDTO.CartItemQuantity;
import com.virtualpet.cart.dto.CartDTO.UpdateQuantityRequest;
import com.virtualpet.cart.service.CartService;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.validation.Valid;
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
 * Per-user cart API. The cart is identified by the authenticated user; creation is lazy on first
 * GET. PUT acts as an upsert against {skuId}, DELETE is idempotent.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

  private final CartService cartService;

  @GetMapping
  public Cart getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
    return cartService.getCart(currentUser.getId());
  }

  @PutMapping("/items/{skuId}")
  public CartItemQuantity putItem(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID skuId,
      @Valid @RequestBody UpdateQuantityRequest request) {
    return cartService.putItem(currentUser.getId(), skuId, request.quantity());
  }

  @DeleteMapping("/items/{skuId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeItem(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID skuId) {
    cartService.removeItem(currentUser.getId(), skuId);
  }
}
