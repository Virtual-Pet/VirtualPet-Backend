package com.virtualpet.shipments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(schema = "logistics", name = "shipment_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "shipment_id", nullable = false)
  private UUID shipmentId;

  @Column(name = "prev_status", nullable = false, length = 50)
  private String prevStatus;

  @Column(name = "new_status", nullable = false, length = 50)
  private String newStatus;

  @Column(length = 100)
  private String reason;

  @Column(name = "modified_by")
  private UUID modifiedBy;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
