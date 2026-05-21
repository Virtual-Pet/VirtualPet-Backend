package com.virtualpet.backend.cart.domain;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cart item stored as JSON inside Redis. Not a JPA entity. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

  private String variantId;
  private String productName;
  private String sku;
  private Map<String, String> attributes;
  private int quantity;
  private BigDecimal unitPrice;
  private String imageUrl;

  /** Derived: unitPrice * quantity */
  public BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
