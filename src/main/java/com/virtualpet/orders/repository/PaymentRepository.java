package com.virtualpet.orders.repository;

import com.virtualpet.orders.domain.PaymentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

  Optional<PaymentEntity> findByProviderPaymentId(String providerPaymentId);

  /** Returns the most recent payment for a session. Handles edge-cases where multiple
   *  payment intents were created for the same session (e.g. user retried checkout). */
  Optional<PaymentEntity> findFirstBySessionIdOrderByCreatedAtDesc(UUID sessionId);

  Optional<PaymentEntity> findByOrderId(UUID orderId);
}
