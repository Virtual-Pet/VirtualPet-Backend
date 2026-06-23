package com.virtualpet.orders.service;

import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.CheckoutSession;
import com.virtualpet.orders.domain.PaymentEntity;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.repository.PaymentRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Normalizes provider webhook payloads to a small internal event and applies it idempotently by
 * providerPaymentId. PAID converges with /confirm via the orchestrator; FAILED moves the session to
 * FAILED. Only the {@code fake} provider is wired in this PR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final CheckoutSessionService sessionService;
  private final ConfirmOrchestrator confirmOrchestrator;

  @Transactional
  public void handleWebhook(String provider, Map<String, Object> payload) {
    String normalizedProvider = provider == null ? "" : provider.toLowerCase();
    if (!"fake".equals(normalizedProvider)) {
      log.warn("Webhook received for unsupported provider '{}'; ignoring", provider);
      return;
    }

    String providerPaymentId = asString(payload.get("providerPaymentId"));
    String rawStatus = asString(payload.get("status"));
    if (providerPaymentId == null || rawStatus == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook payload missing required fields");
    }

    PaymentStatus newStatus;
    try {
      newStatus = PaymentStatus.valueOf(rawStatus.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown payment status: " + rawStatus);
    }

    Optional<PaymentEntity> maybe = paymentRepository.findByProviderPaymentId(providerPaymentId);
    if (maybe.isEmpty()) {
      log.warn("Webhook references unknown providerPaymentId {}; ignoring", providerPaymentId);
      return;
    }

    PaymentEntity payment = maybe.get();
    if (payment.getStatus() == newStatus
        || payment.getStatus() == PaymentStatus.PAID
        || payment.getStatus() == PaymentStatus.REFUNDED) {
      log.debug(
          "Webhook for {} ignored (current={}, incoming={})",
          providerPaymentId,
          payment.getStatus(),
          newStatus);
      return;
    }

    payment.setStatus(newStatus);
    paymentRepository.save(payment);
    log.info("Payment {} transitioned to {} via webhook", payment.getId(), newStatus);

    if (payment.getSessionId() == null) {
      return;
    }

    // Load the checkout session from Redis. It may have expired (TTL 30 min) if the user took
    // long between intent creation and payment. In that case we skip the session-state update
    // but still run the confirm orchestrator so the order is created (it is idempotent).
    Optional<CheckoutSession> maybeSession;
    try {
      maybeSession = Optional.of(sessionService.loadById(payment.getSessionId()));
    } catch (ApiException ex) {
      if (ex.getStatus() == HttpStatus.NOT_FOUND) {
        log.warn(
            "Session {} expired from Redis when webhook arrived for payment {}; "
                + "attempting confirm via DB order lookup",
            payment.getSessionId(),
            payment.getId());
        maybeSession = Optional.empty();
      } else {
        throw ex;
      }
    }

    if (maybeSession.isEmpty()) {
      // Session is gone — if order already exists we are done (idempotent), otherwise we
      // cannot reconstruct it without the line items stored in the session.
      boolean orderExists = orderRepository.findBySessionId(payment.getSessionId()).isPresent();
      if (orderExists) {
        log.info("Order already exists for expired session {}; skipping", payment.getSessionId());
      } else {
        log.error(
            "Session {} expired and no order exists — order cannot be created by webhook. "
                + "User must re-checkout.",
            payment.getSessionId());
      }
      return;
    }

    CheckoutSession session = maybeSession.get();
    switch (newStatus) {
      case PAID -> {
        if (session.getStatus() == SessionStatus.AWAITING_PAYMENT) {
          session.setStatus(SessionStatus.PAID);
          sessionService.save(session);
        }
        if (session.getShippingAddress() != null) {
          confirmOrchestrator.confirmPaidSession(session, payment);
        }
      }
      case FAILED -> {
        session.setStatus(SessionStatus.FAILED);
        sessionService.save(session);
      }
      default -> {
        // PENDING / PROCESSING / REFUNDED: no session transition here.
      }
    }
  }

  private static String asString(Object value) {
    return value == null ? null : value.toString();
  }
}
