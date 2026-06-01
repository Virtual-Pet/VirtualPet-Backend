package com.virtualpet.cart.controller;

import com.virtualpet.cart.dto.CartDTO.CartItemQuantityDTO;
import com.virtualpet.cart.dto.CartDTO.CartViewDTO;
import com.virtualpet.cart.dto.CartDTO.UpdateQuantityRequestDTO;
import com.virtualpet.cart.service.CartService;
import com.virtualpet.cart.web.CartSessionCookie;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart API serving both authenticated and anonymous users through a single set of endpoints.
 *
 * <p>An authenticated request (valid Bearer token) is resolved by user id. Otherwise the cart is
 * identified by the {@code CART_SESSION} cookie. The cookie is created lazily on the first
 * anonymous mutation ({@code PUT}) and returned via {@code Set-Cookie}; the browser then replays it
 * on subsequent requests. On login the guest cart is merged into the user cart and the cookie is
 * cleared (see {@code AuthController}).
 *
 * <p>Cart creation stays lazy: empty carts are never persisted.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final VirtualPetProperties properties;

  @GetMapping
  public CartViewDTO getCart(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @CookieValue(name = CartSessionCookie.NAME, required = false) String sessionId) {
    if (currentUser != null) {
      return cartService.getCart(currentUser.getId());
    }
    if (sessionId != null && !sessionId.isBlank()) {
      return cartService.getAnonCart(sessionId);
    }
    return cartService.emptyCart();
  }

  @PutMapping("/items/{skuId}")
  public CartItemQuantityDTO putItem(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @CookieValue(name = CartSessionCookie.NAME, required = false) String sessionId,
      @PathVariable UUID skuId,
      @Valid @RequestBody UpdateQuantityRequestDTO request,
      HttpServletResponse response) {
    if (currentUser != null) {
      return cartService.putItem(currentUser.getId(), skuId, request.quantity());
    }
    if (sessionId == null || sessionId.isBlank()) {
      sessionId = UUID.randomUUID().toString();
      response.addHeader(
          HttpHeaders.SET_COOKIE,
          CartSessionCookie.create(sessionId, properties.getCart()).toString());
    }
    return cartService.putAnonItem(sessionId, skuId, request.quantity());
  }

  @DeleteMapping("/items/{skuId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeItem(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @CookieValue(name = CartSessionCookie.NAME, required = false) String sessionId,
      @PathVariable UUID skuId) {
    if (currentUser != null) {
      cartService.removeItem(currentUser.getId(), skuId);
    } else if (sessionId != null && !sessionId.isBlank()) {
      cartService.removeAnonItem(sessionId, skuId);
    }
  }
}
