package com.virtualpet.auth.domain;

import com.virtualpet.auth.domain.enums.VehicleType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(schema = "auth", name = "riders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderEntity {

  @Id
  @Column(name = "user_id")
  private UUID userId; // Relacion logica 1:1 a auth.users

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false, length = 50)
  private String lastname;

  @Column(nullable = false, length = 30)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "vehicle_type", nullable = false, length = 30)
  private VehicleType vehicleType;

  @Column(name = "license_plate", length = 15)
  private String licensePlate; // Opcional (p. ej. en bici)

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
