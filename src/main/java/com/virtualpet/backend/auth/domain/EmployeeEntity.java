package com.virtualpet.backend.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(schema = "auth", name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeEntity {

  @Id
  @Column(name = "user_id")
  private UUID userId; // Relacion logica a auth.users

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String lastname;

  @Column(nullable = false, unique = true)
  private String legajo;

  @Column(name = "warehouse_id", nullable = false)
  private Integer warehouseId;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
