package com.virtualpet.orders.service;

import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.idempotency.IdempotencyKeyService;
import com.virtualpet.orders.domain.CheckoutSessionEntity;
import com.virtualpet.orders.domain.PaymentEntity;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponse;
import com.virtualpet.orders.dto.CheckoutDTO.PaymentIntentResponse;
import com.virtualpet.orders.dto.CheckoutDTO.PaymentResponse;
import com.virtualpet.orders.repository.PaymentRepository;
import java.time.Duration;
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

  public record ConfirmOutcome(HttpStatus status, OrderConfirmationResponse body) {}

  private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

  private final PaymentRepository paymentRepository;
  private final CheckoutSessionService sessionService;
  private final ConfirmOrchestrator confirmOrchestrator;
  private final IdempotencyKeyService idempotencyKeys;
  private final FakeProviderService fakeProviderService;
  private final VirtualPetProperties properties;

  /* -------- Create payment intent -------- */

  @Transactional
  public PaymentIntentResponse createIntent(UUID sessionId, UUID userId, String idempotencyKey) {
    return idempotencyKeys.executeOnce(
        "payment-intent",
        idempotencyKey,
        IDEMPOTENCY_TTL,
        PaymentIntentResponse.class,
        () -> createIntentInternal(sessionId, userId, idempotencyKey));
  }

  private PaymentIntentResponse createIntentInternal(
      UUID sessionId, UUID userId, String idempotencyKey) {
    CheckoutSessionEntity session = sessionService.loadOwned(sessionId, userId);
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
    return new PaymentIntentResponse(
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
    CheckoutSessionEntity session = sessionService.loadOwned(sessionId, userId);
    PaymentEntity payment =
        paymentRepository
            .findBySessionId(sessionId)
            .orElseThrow(
                () -> new ApiException(HttpStatus.CONFLICT, "No payment intent for this session"));

    return switch (payment.getStatus()) {
      case PAID -> {
        OrderConfirmationResponse body = confirmOrchestrator.confirmPaidSession(session, payment);
        yield new ConfirmOutcome(HttpStatus.CREATED, body);
      }
      case FAILED -> new ConfirmOutcome(HttpStatus.PAYMENT_REQUIRED, null);
      case PENDING, PROCESSING -> new ConfirmOutcome(HttpStatus.ACCEPTED, null);
      case REFUNDED -> new ConfirmOutcome(HttpStatus.CONFLICT, null);
    };
  }

  /* -------- Read a payment -------- */

  @Transactional(readOnly = true)
  public PaymentResponse getPayment(UUID paymentId, UUID userId) {
    PaymentEntity payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
    if (payment.getUserId() != null && !payment.getUserId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Payment does not belong to this user");
    }
    return new PaymentResponse(
        payment.getId(),
        payment.getProvider(),
        payment.getProviderPaymentId(),
        payment.getStatus(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getOrderId());
  }
}
