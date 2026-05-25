package com.virtualpet.shipments.repository;

import com.virtualpet.shipments.domain.ShipmentStatusHistoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentStatusHistoryRepository
    extends JpaRepository<ShipmentStatusHistoryEntity, UUID> {

  List<ShipmentStatusHistoryEntity> findByShipmentIdOrderByCreatedAtAsc(UUID shipmentId);
}
