package com.virtualpet.shipments.repository;

import com.virtualpet.shipments.domain.ShipmentEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository
    extends JpaRepository<ShipmentEntity, UUID>, JpaSpecificationExecutor<ShipmentEntity> {

  Optional<ShipmentEntity> findByOrderId(UUID orderId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM ShipmentEntity s WHERE s.orderId = :orderId")
  Optional<ShipmentEntity> findByOrderIdForUpdate(@Param("orderId") UUID orderId);
}
