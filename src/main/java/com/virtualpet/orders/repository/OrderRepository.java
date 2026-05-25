package com.virtualpet.orders.repository;

import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository
    extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<OrderEntity> findByIdAndUserId(UUID id, UUID userId);

  Optional<OrderEntity> findBySessionId(UUID sessionId);

  long countByStatus(OrderStatus status);
}
