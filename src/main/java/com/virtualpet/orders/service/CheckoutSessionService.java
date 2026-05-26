package com.virtualpet.orders.service;

import com.virtualpet.cart.domain.Cart;
import com.virtualpet.cart.domain.CartItem;
import com.virtualpet.cart.service.CartService;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.CheckoutSession;
import com.virtualpet.orders.domain.SessionLineItem;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.domain.SessionTotals;
import com.virtualpet.orders.dto.CheckoutDTO.CheckoutSessionResponseDTO;
import com.virtualpet.orders.dto.CheckoutDTO.SetShippingAddressRequestDTO;
import com.virtualpet.orders.repository.CheckoutSessionRedisRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Owns the lifecycle of a CheckoutSession. Sessions are ephemeral and live in Redis with a TTL;
 * only the confirmed Order (post-payment) is persisted in Postgres.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutSessionService {

  private static final Duration SESSION_TTL = Duration.ofMinutes(30);
  private static final String CURRENCY = "ARS";
  private static final BigDecimal SHIPPING = BigDecimal.ZERO;

  private final CheckoutSessionRedisRepository sessionRepository;
  private final CartService cartService;
  private final ProductVariantRepository variantRepository;

  public record StartResult(CheckoutSessionResponseDTO response, boolean created) {}

  public StartResult startCheckout(UUID userId) {
    Optional<CheckoutSession> active = sessionRepository.findActiveByUser(userId);
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

    CheckoutSession session =
        CheckoutSession.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .status(SessionStatus.PENDING)
            .lineItems(lineItems)
            .totals(totals)
            .currency(CURRENCY)
            .expiresAt(Instant.now().plus(SESSION_TTL))
            .build();

    boolean claimed = sessionRepository.tryClaimActive(userId, session.getId(), SESSION_TTL);
    if (!claimed) {
      Optional<CheckoutSession> existing = sessionRepository.findActiveByUser(userId);
      if (existing.isPresent() && !isExpired(existing.get())) {
        log.debug("Lost race claiming active slot; returning existing session {}", existing.get().getId());
        return new StartResult(toResponse(existing.get()), false);
      }
    }

    sessionRepository.save(session);
    log.info("Checkout session created: id={}, userId={}", session.getId(), userId);
    return new StartResult(toResponse(session), true);
  }

  public CheckoutSessionResponseDTO getSession(UUID id, UUID userId) {
    CheckoutSession session = loadOwned(id, userId);
    return toResponse(session);
  }

  public CheckoutSessionResponseDTO setShippingAddress(
      UUID id, UUID userId, SetShippingAddressRequestDTO request) {
    CheckoutSession session = loadOwned(id, userId);
    if (session.getStatus() != SessionStatus.PENDING
        && session.getStatus() != SessionStatus.AWAITING_PAYMENT) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "Shipping address cannot be modified once the session is " + session.getStatus());
    }
    session.setShippingAddress(request.toAddress());
    sessionRepository.save(session);
    return toResponse(session);
  }

  /** Loads a session and asserts the principal owns it. */
  public CheckoutSession loadOwned(UUID id, UUID userId) {
    return sessionRepository
        .findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Checkout session not found"));
  }

  /** Re-exposed for the orchestrator / webhook (no ownership check — internal). */
  public CheckoutSession loadById(UUID id) {
    return sessionRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Checkout session not found"));
  }

  public CheckoutSession save(CheckoutSession session) {
    CheckoutSession saved = sessionRepository.save(session);
    if (isTerminal(session.getStatus())) {
      sessionRepository.clearActive(session.getUserId());
    }
    return saved;
  }

  public CheckoutSessionResponseDTO toResponse(CheckoutSession session) {
    return new CheckoutSessionResponseDTO(
        session.getId(),
        session.getStatus(),
        session.getLineItems(),
        session.getTotals(),
        session.getCurrency(),
        session.getShippingAddress(),
        session.getExpiresAt());
  }

  private boolean isExpired(CheckoutSession session) {
    return session.getExpiresAt() != null && session.getExpiresAt().isBefore(Instant.now());
  }

  private boolean isTerminal(SessionStatus status) {
    return status == SessionStatus.PAID
        || status == SessionStatus.CONFIRMED
        || status == SessionStatus.FAILED
        || status == SessionStatus.EXPIRED;
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
}
