package com.virtualpet.cart.controller;

import com.virtualpet.cart.dto.CartDTO.AddItemRequest;
import com.virtualpet.cart.dto.CartDTO.CartResponse;
import com.virtualpet.cart.dto.CartDTO.UpdateItemRequest;
import com.virtualpet.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart endpoints. The cart is identified by a session token passed in the {@code X-Cart-Session}
 * header. This allows anonymous carts (guest checkout). When the user logs in, the frontend should
 * merge carts by calling add-item for each item in the anonymous session.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @GetMapping
  public ResponseEntity<CartResponse> getCart(
      @RequestHeader(value = "X-Cart-Session", required = false, defaultValue = "anonymous")
          String sessionId) {
    return ResponseEntity.ok(cartService.getCart(sessionId));
  }

  @PostMapping("/items")
  public ResponseEntity<CartResponse> addItem(
      @RequestHeader(value = "X-Cart-Session", required = false, defaultValue = "anonymous")
          String sessionId,
      @RequestBody AddItemRequest request) {
    return ResponseEntity.ok(cartService.addItem(sessionId, request));
  }

  @PatchMapping("/items/{variantId}")
  public ResponseEntity<CartResponse> updateItem(
      @RequestHeader(value = "X-Cart-Session", required = false, defaultValue = "anonymous")
          String sessionId,
      @PathVariable String variantId,
      @RequestBody UpdateItemRequest request) {
    return ResponseEntity.ok(cartService.updateItem(sessionId, variantId, request.quantity()));
  }

  @DeleteMapping("/items/{variantId}")
  public ResponseEntity<CartResponse> removeItem(
      @RequestHeader(value = "X-Cart-Session", required = false, defaultValue = "anonymous")
          String sessionId,
      @PathVariable String variantId) {
    return ResponseEntity.ok(cartService.removeItem(sessionId, variantId));
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart(
      @RequestHeader(value = "X-Cart-Session", required = false, defaultValue = "anonymous")
          String sessionId) {
    cartService.clearCart(sessionId);
    return ResponseEntity.noContent().build();
  }
}
