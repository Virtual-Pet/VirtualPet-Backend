package com.virtualpet.orders.repository;

import com.virtualpet.orders.domain.CheckoutSessionEntity;
import com.virtualpet.orders.domain.SessionStatus;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSessionEntity, UUID> {

  Optional<CheckoutSessionEntity> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(
      UUID userId, Collection<SessionStatus> statuses);

  Optional<CheckoutSessionEntity> findByIdAndUserId(UUID id, UUID userId);
}
