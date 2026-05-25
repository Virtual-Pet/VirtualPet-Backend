package com.virtualpet.orders.service;

import com.virtualpet.cart.domain.Cart;
import com.virtualpet.cart.domain.CartItem;
import com.virtualpet.cart.service.CartService;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.CheckoutSessionEntity;
import com.virtualpet.orders.domain.SessionLineItem;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.domain.SessionTotals;
import com.virtualpet.orders.dto.CheckoutDTO.CheckoutSessionResponse;
import com.virtualpet.orders.dto.CheckoutDTO.SetShippingAddressRequest;
import com.virtualpet.orders.repository.CheckoutSessionRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the lifecycle of a CheckoutSession: build the immutable snapshot from the user's cart, track
 * the address, expose the read view. Payment + confirmation live in their own services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutSessionService {

  private static final Duration SESSION_TTL = Duration.ofMinutes(30);
  private static final String CURRENCY = "ARS";
  private static final BigDecimal SHIPPING = BigDecimal.ZERO;
  private static final EnumSet<SessionStatus> ACTIVE_STATUSES =
      EnumSet.of(SessionStatus.PENDING, SessionStatus.AWAITING_PAYMENT);

  private final CheckoutSessionRepository sessionRepository;
  private final CartService cartService;
  private final ProductVariantRepository variantRepository;

  public record StartResult(CheckoutSessionResponse response, boolean created) {}

  @Transactional
  public StartResult startCheckout(UUID userId) {
    var active =
        sessionRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(userId, ACTIVE_STATUSES);
    if (active.isPresent() && !isExpired(active.get())) {
      log.debug("Reusing active checkout session {}", active.get().getId());
      return new StartResult(toResponse(active.get()), false);
    }

    Cart cart = cartService.getRawCart(userId);
    if (cart.getItems().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
    }

    Map<UUID, ProductVariantEntity> variants = fetchVariants(cart);
    validateAvailability(cart, variants);

    List<SessionLineItem> lineItems =
        cart.getItems().stream()
            .map(
                item -> {
                  BigDecimal unitPrice = variants.get(item.getSkuId()).getPrice();
                  BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                  return new SessionLineItem(
                      item.getSkuId(), item.getQuantity(), unitPrice, subtotal);
                })
            .toList();
    BigDecimal itemsTotal =
        lineItems.stream().map(SessionLineItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    SessionTotals totals = new SessionTotals(itemsTotal, SHIPPING, itemsTotal.add(SHIPPING));

    CheckoutSessionEntity entity =
        CheckoutSessionEntity.builder()
            .userId(userId)
            .status(SessionStatus.PENDING)
            .lineItems(lineItems)
            .totals(totals)
            .currency(CURRENCY)
            .expiresAt(Instant.now().plus(SESSION_TTL))
            .build();
    CheckoutSessionEntity saved = sessionRepository.save(entity);
    log.info("Checkout session created: id={}, userId={}", saved.getId(), userId);
    return new StartResult(toResponse(saved), true);
  }

  @Transactional(readOnly = true)
  public CheckoutSessionResponse getSession(UUID id, UUID userId) {
    CheckoutSessionEntity entity = loadOwned(id, userId);
    return toResponse(entity);
  }

  @Transactional
  public CheckoutSessionResponse setShippingAddress(
      UUID id, UUID userId, SetShippingAddressRequest request) {
    CheckoutSessionEntity entity = loadOwned(id, userId);
    if (entity.getStatus() != SessionStatus.PENDING
        && entity.getStatus() != SessionStatus.AWAITING_PAYMENT) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "Shipping address cannot be modified once the session is " + entity.getStatus());
    }
    entity.setShippingAddress(request.toAddress());
    return toResponse(sessionRepository.save(entity));
  }

  /** Loads a session and asserts the principal owns it. */
  public CheckoutSessionEntity loadOwned(UUID id, UUID userId) {
    CheckoutSessionEntity entity =
        sessionRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "Checkout session not found"));
    return entity;
  }

  public CheckoutSessionResponse toResponse(CheckoutSessionEntity entity) {
    return new CheckoutSessionResponse(
        entity.getId(),
        entity.getStatus(),
        entity.getLineItems(),
        entity.getTotals(),
        entity.getCurrency(),
        entity.getShippingAddress(),
        entity.getExpiresAt());
  }

  private boolean isExpired(CheckoutSessionEntity entity) {
    if (entity.getExpiresAt() == null) {
      return false;
    }
    return entity.getExpiresAt().isBefore(Instant.now());
  }

  private Map<UUID, ProductVariantEntity> fetchVariants(Cart cart) {
    List<UUID> ids = cart.getItems().stream().map(CartItem::getSkuId).toList();
    Map<UUID, ProductVariantEntity> byId =
        variantRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(ProductVariantEntity::getId, v -> v));
    for (UUID id : ids) {
      if (!byId.containsKey(id)) {
        throw new ApiException(HttpStatus.CONFLICT, "Cart references an unknown SKU: " + id);
      }
    }
    return byId;
  }

  private void validateAvailability(Cart cart, Map<UUID, ProductVariantEntity> variants) {
    for (CartItem item : cart.getItems()) {
      ProductVariantEntity variant = variants.get(item.getSkuId());
      if (variant.getStock() < item.getQuantity()) {
        throw new ApiException(
            HttpStatus.CONFLICT,
            "Insufficient stock for SKU "
                + variant.getSku()
                + " (requested "
                + item.getQuantity()
                + ", available "
                + variant.getStock()
                + ")");
      }
    }
  }

  /** Re-exposed for the orchestrator. */
  public CheckoutSessionEntity loadById(UUID id) {
    return sessionRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Checkout session not found"));
  }

  public CheckoutSessionEntity save(CheckoutSessionEntity entity) {
    return sessionRepository.save(entity);
  }
}
