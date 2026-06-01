package com.virtualpet.cart.web;

import com.virtualpet.common.config.VirtualPetProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;

/**
 * Builds the {@code CART_SESSION} cookie used to identify a guest cart. The cookie is issued on the
 * first anonymous mutation and cleared once the cart is merged into the user on login. Shared by
 * {@code CartController} and {@code AuthController} to keep the attributes consistent.
 */
public final class CartSessionCookie {

  public static final String NAME = "CART_SESSION";

  private CartSessionCookie() {}

  /** Cookie carrying the guest session id, living as long as the cart's Redis TTL. */
  public static ResponseCookie create(String sessionId, VirtualPetProperties.Cart cfg) {
    return base(sessionId, cfg).maxAge(Duration.ofHours(cfg.getTtlHours())).build();
  }

  /** Expired cookie ({@code Max-Age=0}) that removes the guest session from the browser. */
  public static ResponseCookie clear(VirtualPetProperties.Cart cfg) {
    return base("", cfg).maxAge(0).build();
  }

  private static ResponseCookie.ResponseCookieBuilder base(
      String value, VirtualPetProperties.Cart cfg) {
    return ResponseCookie.from(NAME, value)
        .httpOnly(true)
        .path("/")
        .sameSite(cfg.getCookie().getSameSite())
        .secure(cfg.getCookie().isSecure());
  }
}
