package com.virtualpet.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for /cart endpoints. Records map 1:1 to schemas defined in docs/api/virtualpet-openapi.yaml.
 */
public final class CartDTO {

  private CartDTO() {}

  public record UpdateQuantityRequest(@NotNull @Min(1) Integer quantity) {}

  public record CartItem(UUID skuId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}

  public record Totals(BigDecimal items, BigDecimal shipping, BigDecimal grandTotal) {}

  public record Cart(List<CartItem> items, Totals totals, String currency) {}

  public record CartItemQuantity(UUID skuId, int quantity) {}
}
