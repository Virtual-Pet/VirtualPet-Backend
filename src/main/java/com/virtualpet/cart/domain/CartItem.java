package com.virtualpet.cart.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cart line stored as JSON inside Redis. Only the SKU reference and quantity are persisted; unit
 * price is re-fetched from the catalog on every read so price changes propagate immediately.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

  private UUID skuId;
  private int quantity;
}