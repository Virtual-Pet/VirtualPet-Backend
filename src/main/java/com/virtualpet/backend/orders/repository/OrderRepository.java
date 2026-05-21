package com.virtualpet.backend.orders.repository;

import com.virtualpet.backend.orders.domain.OrderEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<OrderEntity> findByIdAndUserId(UUID id, UUID userId);
}
