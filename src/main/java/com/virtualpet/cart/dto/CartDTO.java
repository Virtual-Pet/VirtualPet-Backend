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

  public record UpdateQuantityRequestDTO(@NotNull @Min(1) Integer quantity) {}

  public record CartItemDTO(UUID skuId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}

  public record TotalsDTO(BigDecimal items, BigDecimal shipping, BigDecimal grandTotal) {}

  public record CartViewDTO(List<CartItemDTO> items, TotalsDTO totals, String currency) {}

  public record CartItemQuantityDTO(UUID skuId, int quantity) {}
}