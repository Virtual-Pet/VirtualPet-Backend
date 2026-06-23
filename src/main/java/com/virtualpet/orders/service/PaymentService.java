package com.virtualpet.orders.service;

import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.idempotency.IdempotencyKeyService;
import com.virtualpet.orders.domain.CheckoutSession;
import com.virtualpet.orders.domain.PaymentEntity;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponseDTO;
import com.virtualpet.orders.dto.CheckoutDTO.PaymentIntentResponseDTO;
import com.virtualpet.orders.dto.CheckoutDTO.PaymentResponseDTO;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.repository.PaymentRepository;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.repository.ShipmentRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  public record ConfirmOutcome(HttpStatus status, OrderConfirmationResponseDTO body) {}

  private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;
  private final CheckoutSessionService sessionService;
  private final ConfirmOrchestrator confirmOrchestrator;
  private final IdempotencyKeyService idempotencyKeys;
  private final FakeProviderService fakeProviderService;
  private final VirtualPetProperties properties;

  /* -------- Create payment intent -------- */

  @Transactional
  public PaymentIntentResponseDTO createIntent(UUID sessionId, UUID userId, String idempotencyKey) {
    return idempotencyKeys.executeOnce(
        "payment-intent",
        idempotencyKey,
        IDEMPOTENCY_TTL,
        PaymentIntentResponseDTO.class,
        () -> createIntentInternal(sessionId, userId, idempotencyKey));
  }

  private PaymentIntentResponseDTO createIntentInternal(
      UUID sessionId, UUID userId, String idempotencyKey) {
    CheckoutSession session = sessionService.loadOwned(sessionId, userId);
    if (session.getStatus() != SessionStatus.PENDING
        && session.getStatus() != SessionStatus.AWAITING_PAYMENT) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "Cannot create payment intent for session in status " + session.getStatus());
    }
    if (session.getShippingAddress() == null) {
      throw new ApiException(HttpStatus.CONFLICT, "Shipping address is required");
    }

    String provider =
        properties.getPayment().getProvider() == null
            ? "fake"
            : properties.getPayment().getProvider();
    String providerPaymentId = UUID.randomUUID().toString();

    PaymentEntity payment =
        PaymentEntity.builder()
            .userId(userId)
            .sessionId(session.getId())
            .provider(provider)
            .providerPaymentId(providerPaymentId)
            .amount(session.getTotals().grandTotal())
            .currency(session.getCurrency())
            .status(PaymentStatus.PENDING)
            .idempotencyKey(idempotencyKey)
            .build();
    PaymentEntity savedPayment = paymentRepository.save(payment);

    session.setStatus(SessionStatus.AWAITING_PAYMENT);
    sessionService.save(session);

    fakeProviderService.registerPending(providerPaymentId, savedPayment.getId());

    String checkoutUrl =
        properties.getApp().getFrontendUrl()
            + "/fake-checkout?providerPaymentId="
            + providerPaymentId;
    log.info(
        "Payment intent created: paymentId={}, sessionId={}, providerPaymentId={}",
        savedPayment.getId(),
        session.getId(),
        providerPaymentId);
    return new PaymentIntentResponseDTO(
        savedPayment.getId(),
        savedPayment.getProvider(),
        savedPayment.getProviderPaymentId(),
        checkoutUrl,
        savedPayment.getAmount(),
        savedPayment.getCurrency(),
        savedPayment.getStatus());
  }

  /* -------- Confirm a session -------- */

  @Transactional
  public ConfirmOutcome confirm(UUID sessionId, UUID userId, String idempotencyKey) {
    return idempotencyKeys.executeOnce(
        "confirm",
        idempotencyKey,
        IDEMPOTENCY_TTL,
        ConfirmOutcome.class,
        () -> confirmInternal(sessionId, userId));
  }

  private ConfirmOutcome confirmInternal(UUID sessionId, UUID userId) {
    // Fast path: try to load session from Redis.
    // If the session has expired from Redis (TTL elapsed or server restart) but the order was
    // already created (webhook fired before expiry), we fall back to the DB to return the
    // existing order idempotently rather than surfacing a 404 to the user.
    Optional<CheckoutSession> maybeSession;
    try {
      maybeSession = Optional.of(sessionService.loadOwned(sessionId, userId));
    } catch (ApiException ex) {
      if (ex.getStatus() == HttpStatus.NOT_FOUND) {
        log.warn(
            "Session {} not found in Redis (may have expired); checking DB for existing order",
            sessionId);
        maybeSession = Optional.empty();
      } else {
        throw ex;
      }
    }

    // If the session is gone from Redis, check whether an order already exists.
    if (maybeSession.isEmpty()) {
      return orderRepository
          .findBySessionId(sessionId)
          .map(
              order -> {
                UUID shipmentId =
                    shipmentRepository
                        .findByOrderId(order.getId())
                        .map(ShipmentEntity::getId)
                        .orElse(null);
                OrderConfirmationResponseDTO body =
                    new OrderConfirmationResponseDTO(
                        order.getId(), shipmentId, order.getStatus().name(), null);
                return new ConfirmOutcome(HttpStatus.CREATED, body);
              })
          .orElseThrow(
              () ->
                  new ApiException(
                      HttpStatus.NOT_FOUND,
                      "Checkout session expired and no order was found for it"));
    }

    CheckoutSession session = maybeSession.get();
    PaymentEntity payment =
        paymentRepository.findFirstBySessionIdOrderByCreatedAtDesc(sessionId).orElse(null);

    // No payment intent: order placed without upfront payment (cash, transfer, etc.)
    if (payment == null) {
      OrderConfirmationResponseDTO body = confirmOrchestrator.placeOrder(session);
      return new ConfirmOutcome(HttpStatus.CREATED, body);
    }

    return switch (payment.getStatus()) {
      case PAID -> {
        OrderConfirmationResponseDTO body =
            confirmOrchestrator.confirmPaidSession(session, payment);
        yield new ConfirmOutcome(HttpStatus.CREATED, body);
      }
      case FAILED -> new ConfirmOutcome(HttpStatus.PAYMENT_REQUIRED, null);
      case PROCESSING ->
          // Provider is still settling the payment. Leave the session awaiting and tell the
          // client to retry once the webhook resolves the payment to PAID or FAILED.
          new ConfirmOutcome(HttpStatus.ACCEPTED, null);
      case PENDING -> {
        // No webhook arrived yet: auto-approve the pending payment so the order is created
        // immediately without requiring a separate webhook call.
        log.info("Auto-approving payment {} for session {}", payment.getId(), sessionId);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        OrderConfirmationResponseDTO body =
            confirmOrchestrator.confirmPaidSession(session, payment);
        yield new ConfirmOutcome(HttpStatus.CREATED, body);
      }
      case REFUNDED -> new ConfirmOutcome(HttpStatus.CONFLICT, null);
    };
  }

  /* -------- Read a payment -------- */

  @Transactional(readOnly = true)
  public PaymentResponseDTO getPayment(UUID paymentId, UUID userId) {
    PaymentEntity payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
    if (payment.getUserId() != null && !payment.getUserId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Payment does not belong to this user");
    }
    return new PaymentResponseDTO(
        payment.getId(),
        payment.getProvider(),
        payment.getProviderPaymentId(),
        payment.getStatus(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getOrderId());
  }
}
