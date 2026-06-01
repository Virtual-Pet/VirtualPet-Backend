package com.virtualpet.cart.service;

import com.virtualpet.cart.domain.Cart;
import com.virtualpet.cart.domain.CartItem;
import com.virtualpet.cart.dto.CartDTO.CartItemQuantityDTO;
import com.virtualpet.cart.dto.CartDTO.TotalsDTO;
import com.virtualpet.cart.dto.CartDTO.CartViewDTO;
import com.virtualpet.cart.dto.CartDTO.CartItemDTO;
import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
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
import tools.jackson.core.type.TypeReference;
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
  private static final String ANON_KEY_PREFIX = "cart:anon:";
  private static final String CURRENCY = "ARS";
  private static final BigDecimal SHIPPING = BigDecimal.ZERO;
  private static final TypeReference<Map<String, String>> ATTRS_TYPE = new TypeReference<>() {};

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ProductVariantRepository variantRepository;
  private final VirtualPetProperties properties;

  /* ---------- Public API ---------- */

  public CartViewDTO getCart(UUID userId) {
    Cart cart = loadCart(userId);
    return toDto(cart);
  }

  /** Empty cart view, used when there is neither an authenticated user nor a session cookie. */
  public CartViewDTO emptyCart() {
    return toDto(Cart.builder().build());
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

  /* ---------- Anonymous cart ---------- */

  public CartViewDTO getAnonCart(String sessionId) {
    return toDto(loadAnonCart(sessionId));
  }

  public CartItemQuantityDTO putAnonItem(String sessionId, UUID skuId, int quantity) {
    if (quantity < 1) {
      throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Quantity must be at least 1");
    }
    requireVariant(skuId);

    Cart cart = loadAnonCart(sessionId);
    CartItem existing = cart.findItem(skuId);
    if (existing != null) {
      existing.setQuantity(quantity);
    } else {
      cart.getItems().add(CartItem.builder().skuId(skuId).quantity(quantity).build());
    }
    saveAnonCart(sessionId, cart);
    return new CartItemQuantityDTO(skuId, quantity);
  }

  public void removeAnonItem(String sessionId, UUID skuId) {
    Cart cart = loadAnonCart(sessionId);
    boolean removed = cart.getItems().removeIf(i -> skuId.equals(i.getSkuId()));
    if (removed) {
      saveAnonCart(sessionId, cart);
    }
  }

  /** Merges anonymous cart into the user cart, summing quantities for duplicate SKUs. */
  public void mergeAnonCartIntoUser(String sessionId, UUID userId) {
    Cart anon = loadAnonCart(sessionId);
    if (anon.getItems().isEmpty()) return;

    Cart user = loadCart(userId);
    for (CartItem anonItem : anon.getItems()) {
      CartItem existing = user.findItem(anonItem.getSkuId());
      if (existing != null) {
        existing.setQuantity(existing.getQuantity() + anonItem.getQuantity());
      } else {
        user.getItems().add(CartItem.builder()
            .skuId(anonItem.getSkuId())
            .quantity(anonItem.getQuantity())
            .build());
      }
    }
    saveCart(userId, user);
    redisTemplate.delete(anonKey(sessionId));
    log.debug("Anon cart merged into userId={}, sessionId={}", userId, sessionId);
  }

  /* ---------- Internals ---------- */

  private String key(UUID userId) {
    return KEY_PREFIX + userId;
  }

  private String anonKey(String sessionId) {
    return ANON_KEY_PREFIX + sessionId;
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

  private Cart loadAnonCart(String sessionId) {
    String json = redisTemplate.opsForValue().get(anonKey(sessionId));
    if (json == null) {
      return Cart.builder().build();
    }
    try {
      return objectMapper.readValue(json, Cart.class);
    } catch (JacksonException e) {
      log.warn("Failed to deserialize anon cart for sessionId={}, returning empty", sessionId, e);
      return Cart.builder().build();
    }
  }

  private void saveCart(UUID userId, Cart cart) {
    try {
      String json = objectMapper.writeValueAsString(cart);
      redisTemplate
          .opsForValue()
          .set(key(userId), json, properties.getCart().getTtlHours(), TimeUnit.HOURS);
    } catch (JacksonException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist cart");
    }
  }

  private void saveAnonCart(String sessionId, Cart cart) {
    try {
      String json = objectMapper.writeValueAsString(cart);
      redisTemplate
          .opsForValue()
          .set(anonKey(sessionId), json, properties.getCart().getTtlHours(), TimeUnit.HOURS);
    } catch (JacksonException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist cart");
    }
  }

  private ProductVariantEntity requireVariant(UUID skuId) {
    return variantRepository
        .findByIdWithProduct(skuId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SKU not found"));
  }

  private CartViewDTO toDto(Cart cart) {
    if (cart.getItems().isEmpty()) {
      return new CartViewDTO(
          List.of(), new TotalsDTO(BigDecimal.ZERO, SHIPPING, SHIPPING), CURRENCY);
    }

    List<UUID> ids = cart.getItems().stream().map(CartItem::getSkuId).toList();
    Map<UUID, ProductVariantEntity> byId = fetchVariants(ids);

    List<CartItemDTO> dtoItems =
        cart.getItems().stream().map(item -> toItemDto(item, byId.get(item.getSkuId()))).toList();

    BigDecimal itemsTotal =
        dtoItems.stream()
            .map(CartItemDTO::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    TotalsDTO totals = new TotalsDTO(itemsTotal, SHIPPING, itemsTotal.add(SHIPPING));
    return new CartViewDTO(dtoItems, totals, CURRENCY);
  }

  private CartItemDTO toItemDto(
      CartItem item, ProductVariantEntity variant) {
    BigDecimal unitPrice = variant == null ? BigDecimal.ZERO : variant.getPrice();
    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    ProductEntity product = variant == null ? null : variant.getProduct();
    boolean available = variant != null && variant.getStock() >= item.getQuantity();
    return new CartItemDTO(
        item.getSkuId(),
        variant == null ? null : variant.getSku(),
        product == null ? null : product.getId(),
        product == null ? null : product.getName(),
        product == null ? null : product.getBrand(),
        variant == null ? Map.of() : parseAttributes(variant.getAttributes()),
        variant == null ? null : variant.getImageUrl(),
        item.getQuantity(),
        unitPrice,
        subtotal,
        available);
  }

  private Map<UUID, ProductVariantEntity> fetchVariants(Collection<UUID> ids) {
    return variantRepository.findAllByIdInWithProduct(ids).stream()
        .collect(Collectors.toMap(ProductVariantEntity::getId, Function.identity()));
  }

  private Map<String, String> parseAttributes(String json) {
    if (json == null || json.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      return objectMapper.readValue(json, ATTRS_TYPE);
    } catch (Exception ex) {
      log.warn("Failed to parse variant attributes JSON: {}", ex.getMessage());
      return new LinkedHashMap<>();
    }
  }
}
