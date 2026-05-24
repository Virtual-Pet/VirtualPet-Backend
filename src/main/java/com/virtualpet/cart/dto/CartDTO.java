package com.virtualpet.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class CartDTO {

  private CartDTO() {}

  public record AddItemRequest(
      String variantId,
      String productName,
      String sku,
      Map<String, String> attributes,
      int quantity,
      BigDecimal unitPrice,
      String imageUrl) {}

  public record UpdateItemRequest(int quantity) {}

  public record CartItemResponse(
      String variantId,
      String productName,
      String sku,
      Map<String, String> attributes,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal lineTotal,
      String imageUrl) {}

  public record CartResponse(List<CartItemResponse> items, BigDecimal subtotal, int itemCount) {}
}
