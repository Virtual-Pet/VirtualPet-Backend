package com.virtualpet.backend.logistics.repository;

import com.virtualpet.backend.logistics.domain.ShipmentEntity;
import com.virtualpet.backend.logistics.domain.ShipmentStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, UUID> {

    List<ShipmentEntity> findByStatusOrderByCreatedAtAsc(ShipmentStatus status);

    Optional<ShipmentEntity> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShipmentEntity s WHERE s.orderId = :orderId")
    Optional<ShipmentEntity> findByOrderIdForUpdate(@Param("orderId") UUID orderId);
}
