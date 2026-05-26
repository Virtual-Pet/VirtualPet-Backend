package com.virtualpet.cart.service;

import com.virtualpet.cart.domain.Cart;
import com.virtualpet.cart.domain.CartItem;
import com.virtualpet.cart.dto.CartDTO.CartItemQuantityDTO;
import com.virtualpet.cart.dto.CartDTO.TotalsDTO;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Per-user cart persisted in Redis. The Redis value stores only {skuId, quantity}; prices are
 * re-fetched from the catalog on every read so live price changes propagate to the response.
 *
 * <p>Creation is lazy: a GET against a missing key returns an empty cart with totals=0.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

  private static final String KEY_PREFIX = "cart:";
  private static final long TTL_HOURS = 24;
  private static final String CURRENCY = "ARS";
  private static final BigDecimal SHIPPING = BigDecimal.ZERO;

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ProductVariantRepository variantRepository;

  /* ---------- Public API ---------- */

  public com.virtualpet.cart.dto.CartDTO.CartViewDTO getCart(UUID userId) {
    Cart cart = loadCart(userId);
    return toDto(cart);
  }

  public CartItemQuantityDTO putItem(UUID userId, UUID skuId, int quantity) {
    if (quantity < 1) {
      throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Quantity must be at least 1");
    }
    requireVariant(skuId);

    Cart cart = loadCart(userId);
    CartItem existing = cart.findItem(skuId);
    if (existing != null) {
      existing.setQuantity(quantity);
    } else {
      cart.getItems().add(CartItem.builder().skuId(skuId).quantity(quantity).build());
    }
    saveCart(userId, cart);
    return new CartItemQuantityDTO(skuId, quantity);
  }

  public void removeItem(UUID userId, UUID skuId) {
    Cart cart = loadCart(userId);
    boolean removed = cart.getItems().removeIf(i -> skuId.equals(i.getSkuId()));
    if (removed) {
      saveCart(userId, cart);
    }
  }

  /** Returns the raw Cart domain object (used by checkout). */
  public Cart getRawCart(UUID userId) {
    return loadCart(userId);
  }

  public void clearCart(UUID userId) {
    redisTemplate.delete(key(userId));
    log.debug("Cart cleared: userId={}", userId);
  }

  /* ---------- Internals ---------- */

  private String key(UUID userId) {
    return KEY_PREFIX + userId;
  }

  private Cart loadCart(UUID userId) {
    String json = redisTemplate.opsForValue().get(key(userId));
    if (json == null) {
      return Cart.builder().build();
    }
    try {
      return objectMapper.readValue(json, Cart.class);
    } catch (JacksonException e) {
      log.warn("Failed to deserialize cart for userId={}, returning empty", userId, e);
      return Cart.builder().build();
    }
  }

  private void saveCart(UUID userId, Cart cart) {
    try {
      String json = objectMapper.writeValueAsString(cart);
      redisTemplate.opsForValue().set(key(userId), json, TTL_HOURS, TimeUnit.HOURS);
    } catch (JacksonException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist cart");
    }
  }

  private ProductVariantEntity requireVariant(UUID skuId) {
    return variantRepository
        .findByIdWithProduct(skuId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SKU not found"));
  }

  private com.virtualpet.cart.dto.CartDTO.CartViewDTO toDto(Cart cart) {
    if (cart.getItems().isEmpty()) {
      return new com.virtualpet.cart.dto.CartDTO.CartViewDTO(
          List.of(), new TotalsDTO(BigDecimal.ZERO, SHIPPING, SHIPPING), CURRENCY);
    }

    List<UUID> ids = cart.getItems().stream().map(CartItem::getSkuId).toList();
    Map<UUID, ProductVariantEntity> byId = fetchVariants(ids);

    List<com.virtualpet.cart.dto.CartDTO.CartItemDTO> dtoItems =
        cart.getItems().stream()
            .map(
                item -> {
                  ProductVariantEntity variant = byId.get(item.getSkuId());
                  BigDecimal unitPrice = variant == null ? BigDecimal.ZERO : variant.getPrice();
                  BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                  return new com.virtualpet.cart.dto.CartDTO.CartItemDTO(
                      item.getSkuId(), item.getQuantity(), unitPrice, subtotal);
                })
            .toList();

    BigDecimal itemsTotal =
        dtoItems.stream()
            .map(com.virtualpet.cart.dto.CartDTO.CartItemDTO::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    TotalsDTO totals = new TotalsDTO(itemsTotal, SHIPPING, itemsTotal.add(SHIPPING));
    return new com.virtualpet.cart.dto.CartDTO.CartViewDTO(dtoItems, totals, CURRENCY);
  }

  private Map<UUID, ProductVariantEntity> fetchVariants(Collection<UUID> ids) {
    return variantRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(ProductVariantEntity::getId, Function.identity()));
  }
}
