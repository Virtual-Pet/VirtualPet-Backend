package com.virtualpet.cart.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cart state stored as JSON in Redis. Minimal — prices and stock are derived from the catalog. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

  @Builder.Default private List<CartItem> items = new ArrayList<>();

  public CartItem findItem(UUID skuId) {
    return items.stream().filter(i -> skuId.equals(i.getSkuId())).findFirst().orElse(null);
  }
}
