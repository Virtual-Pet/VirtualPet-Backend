package com.virtualpet.orders.repository;

import com.virtualpet.orders.domain.PaymentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

  Optional<PaymentEntity> findByProviderPaymentId(String providerPaymentId);

  Optional<PaymentEntity> findBySessionId(UUID sessionId);

  Optional<PaymentEntity> findByOrderId(UUID orderId);
}
