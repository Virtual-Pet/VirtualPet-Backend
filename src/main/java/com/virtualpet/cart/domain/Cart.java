package com.virtualpet.cart.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents the full cart state stored as JSON in Redis. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

  @Builder.Default private List<CartItem> items = new ArrayList<>();

  public BigDecimal subtotal() {
    return items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public int itemCount() {
    return items.stream().mapToInt(CartItem::getQuantity).sum();
  }

  /** Finds an item by variantId, or null. */
  public CartItem findItem(String variantId) {
    return items.stream().filter(i -> i.getVariantId().equals(variantId)).findFirst().orElse(null);
  }
}
